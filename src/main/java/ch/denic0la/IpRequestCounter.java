package ch.denic0la;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.inject.Inject;
import io.vertx.core.http.HttpServerRequest;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import jakarta.enterprise.context.ApplicationScoped;

@Provider
@ApplicationScoped
public class IpRequestCounter implements ContainerRequestFilter {

    @Inject
    HttpServerRequest request; // Inject the underlying Vert.x request to get the IP

    // Stores IP -> Count
    private final ConcurrentHashMap<String, LongAdder> ipCounts = new ConcurrentHashMap<>();

    @Override
    public void filter(ContainerRequestContext context) {
        String remoteIp = request.remoteAddress().host();

        // Efficiently increment the counter for this IP
        ipCounts.computeIfAbsent(remoteIp, k -> new LongAdder()).increment();
    }

    public long getCountForIp(String ip) {
        LongAdder adder = ipCounts.get(ip);
        return (adder != null) ? adder.sum() : 0;
    }

    public ConcurrentHashMap<String, LongAdder> getAllCounts() {
        return ipCounts;
    }
}