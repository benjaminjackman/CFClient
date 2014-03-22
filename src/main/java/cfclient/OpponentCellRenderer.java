package cfclient;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

class OpponentCellRenderer extends DefaultListCellRenderer {
  private static final long serialVersionUID = 1L;
  private final String opponentName;

  public OpponentCellRenderer(String opponentName) {
    this.opponentName = opponentName;
  }

  @Override
  public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

   // if (value.equals(opponentName)) {
      component.setFont(component.getFont().deriveFont(Font.ITALIC));
      System.out.println("Setting " + opponentName + "to italic");
   // }
    return component;
  }
}