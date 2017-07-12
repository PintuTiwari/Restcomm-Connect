/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package org.restcomm.connect.telephony.api.util;

import static javax.servlet.sip.SipServletResponse.SC_PROXY_AUTHENTICATION_REQUIRED;
import static org.restcomm.connect.commons.util.HexadecimalUtils.toHex;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.sip.SipServletRequest;
import javax.servlet.sip.SipServletResponse;
import javax.servlet.sip.SipURI;

import org.restcomm.connect.dao.ClientsDao;
import org.restcomm.connect.dao.DaoManager;
import org.restcomm.connect.dao.entities.Client;
import org.restcomm.connect.commons.util.DigestAuthentication;

/**
 *
 * Helper class for managing SIP interactions
 *
 *
 * @author ivelin.ivanov@teletax.com
 *
 */
public class CallControlHelper {

    static boolean permitted(final String authorization, final String method, DaoManager daoManager) {
        return permitted(authorization, method, daoManager, "MD5");
    }

    static boolean permitted(final String authorization, final String method, DaoManager daoManager, String algo) {
        final Map<String, String> map = authHeaderToMap(authorization);
        final String user = map.get("username");
        final String algorithm = map.get("algorithm");
        final String realm = map.get("realm");
        final String uri = map.get("uri");
        final String nonce = map.get("nonce");
        final String nc = map.get("nc");
        final String cnonce = map.get("cnonce");
        final String qop = map.get("qop");
        final String response = map.get("response");
        final ClientsDao clients = daoManager.getClientsDao();
        final Client client = clients.getClient(user);
        if (client != null && Client.ENABLED == client.getStatus()) {
            final String password2 = client.getPassword();
            final String result = DigestAuthentication.response(algorithm, user, realm, "", password2, nonce, nc, cnonce,
                    method, uri, null, qop);
            return result.equals(response);
        } else {
            return false;
        }
    }

    /**
     *
     * Check if a client is authenticated. If so, return true. Otherwise request authentication and return false;
     *
     * @return
     * @throws IOException
     */
    public static boolean checkAuthentication(SipServletRequest request, DaoManager storage) throws IOException {
        return checkAuthentication(request, storage, "MD5");
    }

    /**
     *
     * Check if a client is authenticated. If so, return true. Otherwise request authentication and return false;
     * @param algo TODO
     *
     * @return
     * @throws IOException
     */
    public static boolean checkAuthentication(SipServletRequest request, DaoManager storage, String algo) throws IOException {
        // Make sure we force clients to authenticate.
        final String authorization = request.getHeader("Proxy-Authorization");
        final String method = request.getMethod();
        if (authorization == null || !CallControlHelper.permitted(authorization, method, storage, algo)) {
            authenticate(request, algo);
            return false;
        } else {
            return true;
        }
    }

    static void authenticate(final SipServletRequest request) throws IOException {
        authenticate(request, "MD5");
    }

    static void authenticate(final SipServletRequest request, String algo) throws IOException {
        final SipServletResponse response = request.createResponse(SC_PROXY_AUTHENTICATION_REQUIRED);
        final String nonce = nonce();
        final SipURI uri = (SipURI) request.getTo().getURI();
        //TODO: fetch from config
        final String realm = uri.getHost();
        //TODO: loop through algorithms list and add additional headers
        final String header = header(nonce, realm, "Digest", algo);
        response.addHeader("Proxy-Authenticate", header);
        response.send();
    }

    private static Map<String, String> authHeaderToMap(final String header) {
        final Map<String, String> map = new HashMap<String, String>();
        final int endOfScheme = header.indexOf(" ");
        map.put("scheme", header.substring(0, endOfScheme).trim());
        final String[] tokens = header.substring(endOfScheme + 1).split(",");
        for (final String token : tokens) {
            final String[] values = token.trim().split("=",2); //Issue #935, split only for first occurrence of "="
            map.put(values[0].toLowerCase(), values[1].replace("\"", ""));
        }

        return map;
    }

    static String nonce() {
        final byte[] uuid = UUID.randomUUID().toString().getBytes();
        final char[] hex = toHex(uuid);
        return new String(hex).substring(0, 31);
    }

    static String header(final String nonce, final String realm, final String scheme) {
        return header(nonce, realm, scheme, "MD5");
    }

    static String header(final String nonce, final String realm, final String scheme, String algo) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(scheme).append(" ");
        if(!algo.isEmpty()){
            //TODO: check algorithm-session
            buffer.append("algorithm=\"").append(algo).append("\", ");
            //TODO: fetch from config
            buffer.append("qop=\"").append("auth").append("\", ");
        }
        buffer.append("realm=\"").append(realm).append("\", ");
        buffer.append("nonce=\"").append(nonce).append("\"");
        return buffer.toString();
    }

    /**
     *
     * Extracts the User SIP identity from a request header
     *
     * @param request
     * @param useTo Whether or not to use the To field in the SIP header
     * @return
     */
    public static String getUserSipId(final SipServletRequest request, boolean useTo) {
        final SipURI toUri;
        final String toUser;
        if (useTo) {
            toUri = (SipURI) request.getTo().getURI();
            toUser = toUri.getUser();
        } else {
            toUri = (SipURI) request.getRequestURI();
            toUser = toUri.getUser();
        }
        return toUser;
    }

}
