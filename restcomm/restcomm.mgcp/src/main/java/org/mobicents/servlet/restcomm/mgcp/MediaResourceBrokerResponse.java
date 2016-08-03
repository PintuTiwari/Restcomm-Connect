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
package org.mobicents.servlet.restcomm.mgcp;

import org.mobicents.servlet.restcomm.patterns.StandardResponse;

/**
 * @author Maria Farooq (maria.farooq@telestax.com)
 */
public final class MediaResourceBrokerResponse<T> extends StandardResponse<T> {
    public MediaResourceBrokerResponse(final T object) {
        super(object);
    }

    public MediaResourceBrokerResponse(final Throwable cause) {
        super(cause);
    }

    public MediaResourceBrokerResponse(final Throwable cause, final String message) {
        super(cause, message);
    }
}