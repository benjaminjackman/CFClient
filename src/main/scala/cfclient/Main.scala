package cfclient

import java.awt.EventQueue

object Main extends App {

  EventQueue.invokeLater(() => {
    val frame = new MainGUI();
    frame.setVisible(true)
  })

}
