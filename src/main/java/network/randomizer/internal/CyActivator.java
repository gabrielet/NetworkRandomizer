/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.osgi.framework.BundleContext;

/**
 *
 * @author gabriele
 */
public class CyActivator extends AbstractCyActivator {
    
    private CyApplicationManager cyApplicationManager;
    private CySwingApplication cyDesktopService;
    private CyServiceRegistrar cyServiceRegistrar;
    private static final String version = "1.0_thread";
    
    //manca il costruttore CyActivator! quindi di base un CyActivator non prende niente in input e non restituisce nulla in output
    //posso però creare un CyActivator da cui lanciare i metodi Start e get*
    
    @Override
    public void start(BundleContext context) throws Exception {                        
        cyApplicationManager = getService(context, CyApplicationManager.class);
        cyDesktopService = getService(context, CySwingApplication.class);
        cyServiceRegistrar = getService(context, CyServiceRegistrar.class);        
        MenuAction action = new MenuAction("Randomizer "+version, this);        
        Properties properties = new Properties();        
        registerAllServices(context, action, properties);
    }
    
    public CyServiceRegistrar getcyServiceRegistrar() {
        return cyServiceRegistrar;
    }
    
    public CyApplicationManager getcyApplicationManager() {
        return cyApplicationManager;
    }
    
    public CySwingApplication getcyDesktopService() {
        return cyDesktopService;
    }   
}