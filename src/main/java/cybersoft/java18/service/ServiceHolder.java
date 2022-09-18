package cybersoft.java18.service;

public class ServiceHolder {
    private static Service service;
    private ServiceHolder() {
        throw new IllegalStateException("Utility Class");
    }
    public static Service getService() {
        if (service == null) {
            service = new Service();
        }
        return service;
    }
}
