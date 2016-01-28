/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.awt.event.ActionEvent;

//import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
//import org.cytoscape.application.swing.CySwingApplication;

/**
 *
 * @author gabriele
 */

public class MenuAction extends AbstractCyAction {
    //private final  CyApplicationManager cyApplicationManager;
    //private final  CySwingApplication cyDesktopService;
    private final CyActivator cyActivator;
    
    public MenuAction(final String menuTitle, CyActivator activator) {        
        super(menuTitle, activator.getcyApplicationManager(), null, null);
        cyActivator = activator;
        //cyDesktopService = activator.getcyDesktopService();
        //cyApplicationManager = activator.getcyApplicationManager();
        setPreferredMenu("Apps");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("inizializzo il core");
        RandomizerCore randomizerCore = new RandomizerCore(cyActivator);
    }
}