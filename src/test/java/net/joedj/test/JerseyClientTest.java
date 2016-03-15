package net.joedj.test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.glassfish.jersey.process.internal.RequestScope;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class JerseyClientTest {

    @ClassRule
    public static final WireMockClassRule WIREMOCK = new WireMockClassRule();

    private static final Logger REQUEST_SCOPE_LOG;

    static {
        REQUEST_SCOPE_LOG = Logger.getLogger(RequestScope.class.getName());
        REQUEST_SCOPE_LOG.setLevel(Level.FINEST);
        SLF4JBridgeHandler.install();
    }

    @Rule
    public final WireMockClassRule wiremock = WIREMOCK;

    private final Client client = ClientBuilder.newClient();

    @Before
    public void setUp() {
        LogRecorder.clear();
    }

    @Test
    public void testGood() {
        wiremock.stubFor(get(urlPathEqualTo("/200")).willReturn(aResponse().withStatus(200)));
        request("/200");
        assertRequestScopeWasReleased();
    }

    @Test
    public void testExplicitCloseAfterException() {
        try {
            request("/404");
            Assert.fail("Expected NotFoundException");
        } catch (NotFoundException e) {
            e.getResponse().close();
            assertRequestScopeWasReleased();
        }
    }

    @Test
    public void testNoExplicitCloseAfterException() {
        try {
            request("/404");
            Assert.fail("Expected NotFoundException");
        } catch (NotFoundException e) {
            assertRequestScopeWasReleased();
        }
    }

    private static void assertRequestScopeWasReleased() {
        for (ILoggingEvent event : LogRecorder.events()) {
            if (RequestScope.class.getName().equals(event.getLoggerName()) &&
                    event.getMessage().contains("Released scope instance")) {
                return;
            }
        }
        Assert.fail("Expected release log message was not found.");
    }

    private void request(String uri) {
        client.target("http://localhost:" + wiremock.port())
                .path(uri)
                .request(MediaType.TEXT_PLAIN_TYPE)
                .get(String.class);
    }

}
