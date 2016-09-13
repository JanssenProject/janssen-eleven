/*
 * oxEleven is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2016, Gluu
 */

package org.gluu.oxeleven.rest;

import com.google.common.base.Strings;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.gluu.oxeleven.model.Configuration;
import org.gluu.oxeleven.service.ConfigurationService;
import org.gluu.oxeleven.service.PKCS11Service;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.Map;

import static org.gluu.oxeleven.model.DeleteKeyResponseParam.DELETED;

/**
 * @author Javier Rojas Blum
 * @version May 20, 2016
 */
@Name("deleteKeyRestService")
public class DeleteKeyRestServiceImpl implements DeleteKeyRestService {

    private static final Log LOG = Logging.getLog(DeleteKeyRestServiceImpl.class);

    public Response sign(String alias) {
        Response.ResponseBuilder builder = Response.ok();

        try {
            if (Strings.isNullOrEmpty(alias)) {
                builder = Response.status(Response.Status.BAD_REQUEST);
                builder.entity("The request asked for an operation that cannot be supported because the alias parameter is mandatory.");
            } else {
                Configuration configuration = ConfigurationService.instance().getConfiguration();
                String pkcs11Pin = configuration.getPkcs11Pin();
                Map<String, String> pkcs11Config = configuration.getPkcs11Config();

                PKCS11Service pkcs11 = new PKCS11Service(pkcs11Pin, pkcs11Config);

                PublicKey publicKey = pkcs11.getPublicKey(alias);
                if (publicKey == null) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DELETED, false);

                    builder.entity(jsonObject.toString());
                } else {
                    pkcs11.deleteKey(alias);

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(DELETED, true);

                    builder.entity(jsonObject.toString());
                }
            }
        } catch (CertificateException e) {
            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            LOG.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            LOG.error(e.getMessage(), e);
        } catch (KeyStoreException e) {
            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            LOG.error(e.getMessage(), e);
        } catch (IOException e) {
            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            LOG.error(e.getMessage(), e);
        } catch (JSONException e) {
            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            LOG.error(e.getMessage(), e);
        } catch (Exception e) {
            builder = Response.status(Response.Status.INTERNAL_SERVER_ERROR);
            LOG.error(e.getMessage(), e);
        }

        CacheControl cacheControl = new CacheControl();
        cacheControl.setNoTransform(false);
        cacheControl.setNoStore(true);
        builder.cacheControl(cacheControl);
        builder.header("Pragma", "no-cache");
        return builder.build();
    }
}
