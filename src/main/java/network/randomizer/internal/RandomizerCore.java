/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.util.Properties;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;

/**
 *
 * @author gabriele
 */

//questa classe fornisce tutte le funzionalità che riguardano l'applicazione e l'accesso ai menu di input e di output

public final class RandomizerCore {

    public CyApplicationManager cyApplicationManager;
    public CySwingApplication cyDesktopService;
    public CyServiceRegistrar cyServiceRegistrar;
    public CyActivator cyActivator;
    public OptionsMenu optionsmenu;
    public CyNetworkView currentnetworkview;
    public CyNetwork currentnetwork;
    
    public RandomizerCore(CyActivator activator) {
        cyActivator = activator;
        cyServiceRegistrar = activator.getcyServiceRegistrar();
        cyDesktopService = activator.getcyDesktopService();
        cyApplicationManager = activator.getcyApplicationManager();
        currentnetwork = cyApplicationManager.getCurrentNetwork();
        currentnetworkview = cyApplicationManager.getCurrentNetworkView();
        optionsmenu = createOptionsMenu();
    }
    
    public OptionsMenu createOptionsMenu() {
        OptionsMenu optmenu = new OptionsMenu(this);
        cyServiceRegistrar.registerService(optmenu, CytoPanelComponent.class, new Properties());
        CytoPanel cytopanelwest = cyDesktopService.getCytoPanel(CytoPanelName.WEST);
        int index = cytopanelwest.indexOfComponent(optmenu);
        cytopanelwest.setSelectedIndex(index);
        return optmenu;
    }
    
    public void closeOptionsMenu() {
        cyServiceRegistrar.unregisterService(optionsmenu, CytoPanelComponent.class);
    }
    
}