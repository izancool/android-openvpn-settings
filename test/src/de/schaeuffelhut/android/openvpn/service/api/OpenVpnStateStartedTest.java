package de.schaeuffelhut.android.openvpn.service.api;

/**
 * @author Friedrich Schäuffelhut
 * @since 2012-10-28
 */
public class OpenVpnStateStartedTest extends OpenVpnStateTestBase<OpenVpnState.Started>
{
    private static final String DUMMY_STATE = "AUTH";
    private static final String DUMMY_CONNECTED_TO = "USA1";
    private static final String DUMMY_IP = "192.168.1.1";
    private static final long DUMMY_BYTES_SENT = 1036847L;
    private static final long DUMMY_BYTES_RECEIVED = 54398530925L;
    private static final int DUMMY_CONNECTED_SECONDS = 61;


    public OpenVpnStateStartedTest(){
        super( OpenVpnState.Started.class );
    }

    @Override
    public void test_isStarted()
    {
        assertTrue( createOpenVpnState().isStarted() );
    }

    @Override
    public void test_getState()
    {
        for(String state : new String[] { "CONNECTING", "WAIT", "AUTH", "GET_CONFIG", "ASSIGN_IP", "ADD_ROUTES", "CONNECTED", "RECONNECTING", "EXITING" } )
            assertEquals( state, copy( new OpenVpnState.Started( state, DUMMY_CONNECTED_TO, DUMMY_IP, DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getState() );
    }

    public void test_getConnectedTo_USA1()
    {
            assertEquals( "USA1", copy( new OpenVpnState.Started( DUMMY_STATE, "USA1", DUMMY_IP, DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getConnectedTo() );
    }

    public void test_getConnectedTo_France()
    {
            assertEquals( "France", copy( new OpenVpnState.Started( DUMMY_STATE, "France", DUMMY_IP, DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getConnectedTo() );
    }

    @Override
    public void test_getIp()
    {
        assertEquals( "192.168.1.1", copy( new OpenVpnState.Started( DUMMY_STATE, DUMMY_CONNECTED_TO, "192.168.1.1", DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getIp() );
    }

    public void test_getIp_2()
    {
        assertEquals( "172.24.2.5", copy( new OpenVpnState.Started( DUMMY_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getIp() );
    }

    public void test_getBytesSent_0()
    {
        assertEquals( 0, copy( new OpenVpnState.Started( DUMMY_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", 0, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getBytesSent() );
    }

    public void test_getBytesSent_87981534597980()
    {
        assertEquals( 87981534597980L, copy( new OpenVpnState.Started( DUMMY_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", 87981534597980L, DUMMY_BYTES_RECEIVED, DUMMY_CONNECTED_SECONDS ) ).getBytesSent() );
    }

    public void test_getBytesReceived_0()
    {
        assertEquals( 0, copy( new OpenVpnState.Started( DUMMY_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", DUMMY_BYTES_SENT, 0, DUMMY_CONNECTED_SECONDS ) ).getBytesReceived() );
    }

    public void test_getBytesReceived_87981534597980()
    {
        assertEquals( 87981534597980L, copy( new OpenVpnState.Started( DUMMY_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", DUMMY_BYTES_SENT, 87981534597980L, DUMMY_CONNECTED_SECONDS ) ).getBytesReceived() );
    }

    public void test_getConnectedSeconds_0()
    {
        assertEquals( 0, copy( new OpenVpnState.Started( DUMMY_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, 0 ) ).getConnectedSeconds() );
    }

    public void test_getConnectedSeconds_31536000()
    {
        assertEquals( 31536000, copy( new OpenVpnState.Started( DUMMY_STATE, DUMMY_CONNECTED_TO, "172.24.2.5", DUMMY_BYTES_SENT, DUMMY_BYTES_RECEIVED, 31536000 ) ).getConnectedSeconds() );
    }



    @Override
    protected OpenVpnState.Started createOpenVpnState()
    {
        return new OpenVpnState.Started();
    }
}
