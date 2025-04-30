package workshop.es;

import akka.javasdk.DependencyProvider;
import akka.javasdk.ServiceSetup;
import akka.javasdk.annotations.Setup;
import workshop.es.base.application.UTCClock;

@Setup
public class WalletSetup implements ServiceSetup {

    @Override
    public DependencyProvider createDependencyProvider() {
        return DependencyProvider.single(new UTCClock());
    }
}
