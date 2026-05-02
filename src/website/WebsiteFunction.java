package website;

import clojure.java.api.Clojure;
import clojure.lang.IFn;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;
import java.util.Optional;

public class WebsiteFunction {

    private static final IFn handle;

    static {
        IFn require = Clojure.var("clojure.core", "require");
        require.invoke(Clojure.read("website.routes"));
        handle = Clojure.var("website.routes", "handle");
    }

    @FunctionName("Pages")
    public HttpResponseMessage pages(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "{*path}")
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {
        return (HttpResponseMessage) handle.invoke(request);
    }

    @FunctionName("Health")
    public HttpResponseMessage health(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET},
                    authLevel = AuthorizationLevel.ANONYMOUS,
                    route = "health")
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "text/plain; charset=utf-8")
                .body("ok")
                .build();
    }
}
