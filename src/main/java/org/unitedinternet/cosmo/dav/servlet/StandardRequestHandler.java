/*
 * Copyright 2007 Open Source Applications Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.unitedinternet.cosmo.dav.servlet;

import carldav.exception.resolver.ExceptionResolverHandler;
import carldav.exception.resolver.ResponseUtils;
import org.apache.abdera.util.EntityTag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.HttpRequestHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.unitedinternet.cosmo.dav.*;
import org.unitedinternet.cosmo.dav.impl.DavCalendarResource;
import org.unitedinternet.cosmo.dav.provider.BaseProvider;
import org.unitedinternet.cosmo.dav.provider.CalendarResourceProvider;
import org.unitedinternet.cosmo.server.ServerConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * <p>
 * An implementation of the Spring {@link HttpRequestHandler} that
 * services WebDAV requests. Finds the resource being acted upon, checks that
 * conditions are right for the request and resource, chooses a provider
 * based on the resource type, and then delegates to a specific provider
 * method based on the request method.
 * </p>
 */
@Transactional
public class StandardRequestHandler extends AbstractController implements ServerConstants {

    private static final Log LOG = LogFactory.getLog(StandardRequestHandler.class);

    private final DavResourceLocatorFactory locatorFactory;
    private final DavResourceFactory resourceFactory;
    private final ExceptionResolverHandler exceptionResolverHandler;

    public StandardRequestHandler(final DavResourceLocatorFactory locatorFactory, final DavResourceFactory resourceFactory, final ExceptionResolverHandler exceptionResolverHandler) {
        super.setSupportedMethods(null);
        Assert.notNull(locatorFactory, "locatorFactory is null");
        Assert.notNull(locatorFactory, "locatorFactory is null");
        Assert.notNull(resourceFactory, "resourceFactory is null");
        Assert.notNull(exceptionResolverHandler, "exceptionResolverHandler is null");
        this.locatorFactory = locatorFactory;
        this.resourceFactory = resourceFactory;
        this.exceptionResolverHandler = exceptionResolverHandler;
    }

    @Override
    protected ModelAndView handleRequestInternal(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        try {
            final WebDavResource resource = resolveTarget(request);
            preconditions(request, response, resource);
            process(request, response, resource);
        } catch(NotFoundException e){
            LOG.info("404 was happened");
            final CosmoDavException de = exceptionResolverHandler.resolve(e);
            ResponseUtils.sendDavError(de, response);
        } catch (Exception e) {
            final CosmoDavException de = exceptionResolverHandler.resolve(e);
            // We need a way to differentiate exceptions that are "expected" so that the
            // logs don't get too polluted with errors.  For example, OptimisticLockingFailureException
            // is expected and should be handled by the retry logic that is one layer above.
            // Although not ideal, for now simply check for this type and log at a different level.
            LOG.error("error (" + de.getErrorCode() + "): " + de.getMessage(), de);
            ResponseUtils.sendDavError(de, response);
        }
        return null;
    }

    // our methods

    /**
     * <p>
     * Validates preconditions that must exist before the request may be
     * executed. If a precondition is specified but is not met, the
     * appropriate response is set and <code>false</code> is returned.
     * </p>
     * <p>
     * These preconditions are checked:
     * </p>
     * <ul>
     * <li>The <code>If-Match</code> request header</li>
     * <li>The <code>If-None-Match</code> request header</li>
     * <li>The <code>If-Modified-Since</code> request header</li>
     * <li>The <code>If-Unmodified-Since</code> request header</li>
     * </ul>
     */
    protected void preconditions(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws CosmoDavException, IOException {
        ifMatch(request, response, resource);
        ifNoneMatch(request, response, resource);
        ifModifiedSince(request, resource);
        ifUnmodifiedSince(request, resource);
    }

    /**
     * <p>
     * Hands the request off to a provider method for handling. The provider
     * is created by calling {@link #createProvider(WebDavResource)}. The
     * specific provider method is chosen by examining the request method.
     * </p>
     */
    protected void process(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws IOException, CosmoDavException {
        BaseProvider provider = createProvider(resource);

        if (request.getMethod().equals("OPTIONS")) {
            options(response, resource);
        }
        else if (request.getMethod().equals("GET")) {
            provider.get(request, response, resource);
        }
        else if (request.getMethod().equals("HEAD")) {
            provider.head(request, response, resource);
        }
        else if (request.getMethod().equals("PROPFIND")) {
            provider.propfind(request, response, resource);
        }
        else if (request.getMethod().equals("DELETE")) {
            provider.delete(request, response, resource);
        }
        else if (request.getMethod().equals("REPORT")) {
            provider.report(request, response, resource);
        }
        else {
            if (resource.isCollection()) {
                throw new MethodNotAllowedException(request.getMethod() + " not allowed for a collection");
            } else {
                if (request.getMethod().equals("PUT")) {
                    provider.put(request, response, resource);
                }
                else {
                    throw new MethodNotAllowedException(request.getMethod() + " not allowed for a non-collection resource");
                }
            }
        }
    }

    protected BaseProvider createProvider(WebDavResource resource) {
        if (resource instanceof DavCalendarResource) {
            return new CalendarResourceProvider(resourceFactory);
        }
        return new BaseProvider(resourceFactory);
    }

    /**
     * <p>
     * Creates an instance of <code>WebDavResource</code> representing the
     * resource targeted by the request.
     * </p>
     */
    protected WebDavResource resolveTarget(HttpServletRequest request) throws CosmoDavException {
        return resourceFactory.resolve(locatorFactory.createResourceLocatorFromRequest(request), request);
    }

    private void ifMatch(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws CosmoDavException, IOException {
        EntityTag[] requestEtags = EntityTag.parseTags(request.getHeader("If-Match"));
        if (requestEtags.length == 0) {
            return;
        }

        EntityTag resourceEtag = etag(resource);
        if (resourceEtag == null) {
            return;
        }

        if (EntityTag.matchesAny(resourceEtag, requestEtags)) {
            return;
        }

        response.setHeader("ETag", resourceEtag.toString());

        throw new PreconditionFailedException("If-Match disallows conditional request");
    }

    private void ifNoneMatch(HttpServletRequest request, HttpServletResponse response, WebDavResource resource) throws CosmoDavException, IOException {
        EntityTag[] requestEtags = EntityTag.parseTags(request.getHeader("If-None-Match"));
        if (requestEtags.length == 0) {
            return;
        }

        EntityTag resourceEtag = etag(resource);
        if (resourceEtag == null) {
            return;
        }

        if (! EntityTag.matchesAny(resourceEtag, requestEtags)) {
            return;
        }

        response.addHeader("ETag", resourceEtag.toString());

        if (deservesNotModified(request)) {
            throw new NotModifiedException();
        }

        throw new PreconditionFailedException("If-None-Match disallows conditional request");
    }

    private void ifModifiedSince(HttpServletRequest request, WebDavResource resource) throws CosmoDavException, IOException {
        long mod = resource.getModificationTime();
        if (mod == -1) {
            return;
        }
        mod = mod / 1000 * 1000;

        long since = request.getDateHeader("If-Modified-Since");
        if (since == -1) {
            return;
        }

        if (mod > since) {
            return;
        }

        throw new NotModifiedException();
    }

    private void ifUnmodifiedSince(HttpServletRequest request, WebDavResource resource) throws CosmoDavException, IOException {

        long mod = resource.getModificationTime();
        if (mod == -1) {
            return;
        }
        mod = mod / 1000 * 1000;

        long since = request.getDateHeader("If-Unmodified-Since");
        if (since == -1) {
            return;
        }

        if (mod <= since) {
            return;
        }

        throw new PreconditionFailedException("If-Unmodified-Since disallows conditional request");
    }

    private EntityTag etag(WebDavResource resource) {
        String etag = resource.getETag();
        if (etag == null) {
            return null;
        }
        //TODO resource etags have doublequotes wrapped around them
        //if (etag.startsWith("\"")) {
        etag = etag.substring(1, etag.length()-1);
        //}
        return new EntityTag(etag);
    }

    private boolean deservesNotModified(HttpServletRequest request) {
        return "GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod());
    }

    private void options(HttpServletResponse response, WebDavResource resource) {
        response.setStatus(200);
        response.addHeader("Allow", resource.getSupportedMethods());
        response.addHeader("DAV", resource.getComplianceClass());
    }

    @Override
    public ModelAndView handleRequest(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        checkRequest(request);
        prepareResponse(response);
        return handleRequestInternal(request, response);
    }
}
