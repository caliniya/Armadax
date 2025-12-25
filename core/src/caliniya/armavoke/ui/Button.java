package caliniya.armavoke.ui;

import arc.Events;
import arc.scene.style.Drawable;
import arc.util.Align;
import arc.scene.ui.ImageButton;
import caliniya.armavoke.base.type.EventType;

public class Button extends ImageButton {
  public Button(String text, Runnable listener) {
    super();
    clicked(listener);
    row();
    add(text).growX().wrap().center().get().setAlignment(Align.center, Align.center);
    setStyle(Styles.ibuttondef);
  }
}
