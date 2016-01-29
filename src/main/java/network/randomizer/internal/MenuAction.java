/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package network.randomizer.internal;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;

/**
 *
 * @author gabriele
 */

public class MenuAction extends AbstractCyAction {
    private final CyActivator cyActivator;
    
    public MenuAction(final String menuTitle, CyActivator activator) {        
        super(menuTitle, activator.getcyApplicationManager(), null, null);
        cyActivator = activator;
        setPreferredMenu("Apps");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("Initializing the core");
        RandomizerCore randomizerCore = new RandomizerCore(cyActivator);
    }
}