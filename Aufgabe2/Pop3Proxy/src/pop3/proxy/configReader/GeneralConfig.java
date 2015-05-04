package pop3.proxy.configReader;

/**
 * Created by torben on 02.05.15.
 */
public interface GeneralConfig {
    public int getServerport();
    public int getMaxServerConnections();
    public int getServerTimeout();
    public int getMaxSignsPerLineServer();

    public int getMaxClientConnections();
    public int getClientTimeout();
    public int getMaxSignsPerLineClient();
    public int getMaxMailSize();

}
