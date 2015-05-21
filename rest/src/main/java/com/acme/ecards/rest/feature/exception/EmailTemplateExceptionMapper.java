/*
 * Copyright 2015 Acme Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acme.ecards.rest.feature.exception;

import com.acme.ecards.api.template.EmailTemplateException;
import javax.ws.rs.core.Response;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.status;
import javax.ws.rs.ext.ExceptionMapper;

/**
 *
 * @author Sharmarke Aden (saden)
 */
public class EmailTemplateExceptionMapper implements ExceptionMapper<EmailTemplateException> {

    @Override
    public Response toResponse(EmailTemplateException exception) {
        return status(BAD_REQUEST).build();
    }

}
