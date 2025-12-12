package caliniya.armadax.core;

import arc.ApplicationListener;
import arc.assets.Loadable;

import static arc.Core.*;
import caliniya.armadax.ui.fragment.MenuFragment;

public class UI{
	
  public static void Menu(){
    scene.clear();
    new MenuFragment().build(scene.root);
  }
}