package cfclient;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;
import java.net.ConnectException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

public class MainGUI extends JFrame {
  private static final long serialVersionUID = 1L;

  private Connection connection;

  private final JPanel mainPanel;
  private JPanel contentPanel;

  private final JTextField playerNameTextField;

  private final JLabel addressLabel;
  private final JTextField addressTextField;

  private final JLabel portLabel;
  private final JTextField portTextField;

  private final JButton connectButton;

  private final JTextArea debugTextArea;

  private final JList<String> opponentList = new JList<>();

  private final JTextArea chatTextArea;
  private final JTextField chatTextField;

  private final Color playerColor = new Color(0, 0, 0);
  private final Color opponentColor = new Color(255, 0, 0);

  public MainGUI() {
    // setSeaGlassLAF();
    setNimbusLAF();

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    addWindowListener(windowCloseEventListener());
    setBounds(100, 100, 640, 480);
    setResizable(false);
    setAlwaysOnTop(true);
    setLocationByPlatform(true);

    setTitle("Connect Four Client");
    mainPanel = new JPanel();
    mainPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    setContentPane(mainPanel);
    mainPanel.setLayout(null);

    JLabel playerNameLabel = new JLabel("Name");
    playerNameLabel.setBounds(12, 17, 40, 15);
    mainPanel.add(playerNameLabel);

    playerNameTextField = new JTextField();
    playerNameTextField.setText("MyName");
    playerNameTextField.setBounds(60, 11, 122, 27);
    playerNameTextField.setColumns(10);
    playerNameTextField.addKeyListener(playerNameTextFieldKeyListener());
    mainPanel.add(playerNameTextField);

    addressLabel = new JLabel("Address");
    addressLabel.setBounds(210, 12, 60, 25);
    mainPanel.add(addressLabel);

    addressTextField = new JTextField();
    addressTextField.setText("127.0.0.1");
    addressTextField.setBounds(269, 12, 110, 25);
    mainPanel.add(addressTextField);
    addressTextField.setColumns(10);

    portLabel = new JLabel("Port");
    portLabel.setBounds(405, 12, 40, 25);
    mainPanel.add(portLabel);

    portTextField = new JTextField();
    portTextField.setText("50123");
    portTextField.setBounds(440, 12, 60, 25);
    portTextField.setColumns(10);
    portTextField.addKeyListener(portTextFieldKeyListener());
    mainPanel.add(portTextField);

    connectButton = new JButton("Connect");
    connectButton.setBounds(520, 12, 100, 25);
    connectButton.addActionListener(connectButtonActionListener());
    mainPanel.add(connectButton);

    JSeparator separator = new JSeparator();
    separator.setBounds(12, 49, 608, 6);
    mainPanel.add(separator);

    debugTextArea = new JTextArea();
    debugTextArea.setBounds(12, 340, 608, 100);
    debugTextArea.setEditable(false);
    debugTextArea.setEnabled(false);
    mainPanel.add(debugTextArea);

    chatTextArea = new JTextArea();
    chatTextArea.setEditable(false);
    chatTextArea.setWrapStyleWord(true);
    chatTextArea.setLineWrap(true);
    chatTextArea.setBounds(320, 67, 300, 230);
    JScrollPane chatTextAreaScrollPane = new JScrollPane(chatTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    chatTextAreaScrollPane.setBounds(320, 67, 300, 230);
    mainPanel.add(chatTextAreaScrollPane);

    chatTextField = new JTextField();
    chatTextField.setEnabled(false);
    chatTextField.addKeyListener(chatTextFieldKeyListener(chatTextArea, playerNameTextField.getText()));
    chatTextField.setBounds(320, 302, 300, 25);
    mainPanel.add(chatTextField);
    chatTextField.setColumns(10);

    contentPanel = new LobbyGUI();
    mainPanel.add(contentPanel);
    opponentList.addMouseListener(opponentListRightClickListener());
    opponentList.setBounds(0, 0, 300, 230);

  }

  // Listeners

  private ActionListener challengeListener() {
    return new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        String opponentName = opponentList.getSelectedValue();
        System.out.println(opponentName);
        if (opponentName != null)
          challenge(opponentName);
      }
    };
  }

  private WindowAdapter windowCloseEventListener() {
    return new java.awt.event.WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent winEvt) {
        disconnect();
      }
    };
  }

  private MouseAdapter opponentListRightClickListener() {
    return new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.isPopupTrigger()) {
          int index = opponentList.locationToIndex(e.getPoint());
          System.out.println("Row: " + index);
          opponentList.setSelectedIndex(index);

          JPopupMenu menu = new JPopupMenu();
          JMenuItem item = new JMenuItem("Say hello");
          item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              JOptionPane.showMessageDialog(MainGUI.this, "Hello " + opponentList.getSelectedValue());
            }
          });
          menu.add(item);
          menu.show(MainGUI.this, 5, opponentList.getCellBounds(opponentList.getSelectedIndex() + 1, opponentList.getSelectedIndex() + 1).y);
        }
      }
    };
  }

  private KeyAdapter playerNameTextFieldKeyListener() {
    return new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (GUIHelper.isValidPlayerName(playerNameTextField.getText()) && GUIHelper.isValidPort((portTextField.getText()))) {
          connectButton.setEnabled(true);
          playerNameTextField.setBackground(new Color(255, 255, 255));
        } else {
          connectButton.setEnabled(false);
          playerNameTextField.setBackground(new Color(255, 240, 240));
          System.out.println("INVALID player name: " + playerNameTextField.getText());
        }

      }
    };
  }

  private KeyAdapter portTextFieldKeyListener() {
    return new KeyAdapter() {
      @Override
      public void keyReleased(KeyEvent e) {
        if (GUIHelper.isValidPort((portTextField.getText())) && GUIHelper.isValidPlayerName(playerNameTextField.getText())) {
          connectButton.setEnabled(true);
          portTextField.setBackground(new Color(255, 255, 255));
        } else {
          connectButton.setEnabled(false);
          portTextField.setBackground(new Color(255, 240, 240));
          System.out.println("INVALID port: " + portTextField.getText());
        }

      }
    };
  }

  private KeyAdapter chatTextFieldKeyListener(final JTextArea chatTextArea, final String playerName) {
    return new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        String chatMessage = chatTextField.getText();
        if (e.getKeyCode() == KeyEvent.VK_ENTER && GUIHelper.isValidChatMessage(chatMessage)) {
          connection.send(new SendChatMessage(chatMessage));
          chatTextField.setText(null);
        }
      }
    };
  }

  private ActionListener connectButtonActionListener() {
    return new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("Button clicked!");
        toggleConnect();
      }
    };
  }

  // Helper methods

  public void replaceOpponentListWithGameGrid(String opponentName) {
    connectButton.setText("Giveup");
    mainPanel.remove(contentPanel);
    contentPanel = new GameGUI(opponentName);
    // contentPanel.setDoubleBuffered(true);
    contentPanel.setVisible(true);
    mainPanel.add(contentPanel);
    mainPanel.repaint();
  }

  public void replaceGameGridWithOpponentList() {
    connectButton.setText("Disconnect");
    mainPanel.remove(contentPanel);
    contentPanel = new LobbyGUI();
    contentPanel.setVisible(true);
    mainPanel.add(contentPanel);
    mainPanel.repaint();
  }

  public void addChatMessage(String playerName, String chatMessage) {
    chatTextArea.append(playerName + ": " + chatMessage + "\n");
  }

  public void signalErrorNameTaken() {
    JOptionPane.showMessageDialog(this, "The name " + playerNameTextField.getText() + " is already taken!", "Name Taken!", JOptionPane.ERROR_MESSAGE);
    connectButton.setText("Connect");
    enableNameAddressPortTextFields();
    chatTextField.setEnabled(false);
    opponentList.removeAll();
    mainPanel.repaint();
  }

  public void signalChallengeBy(String opponentName) {
    connection.pendingReceivedChallenges().add(opponentName);
    Object[] options = { "Accept challenge", "Deny challenge" };
    int choice = JOptionPane.showOptionDialog(this, opponentName + " challenged you!", "New Challenge!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    if (choice == JOptionPane.YES_OPTION) {
      connection.acceptChallenge(opponentName);
      replaceOpponentListWithGameGrid(opponentName);
    } else {
      connection.denyChallenge(opponentName);
    }
  }

  public void signalChallengeDeniedBy(String opponentName) {
    connection.pendingSentChallenges().remove(opponentName);
    JOptionPane.showMessageDialog(this, opponentName + " denied your challenge!", "Challenge Denied!", JOptionPane.INFORMATION_MESSAGE);
  }

  public void signalChallengeRevoked(String opponentName) {
    connection.pendingReceivedChallenges().remove(opponentName);
    JOptionPane.showMessageDialog(this, opponentName + " has revoked his challenge!", "Challenge Revoked!", JOptionPane.INFORMATION_MESSAGE);
  }

  public void signalGaveup(String opponentName) {
    replaceGameGridWithOpponentList();
    JOptionPane.showMessageDialog(this, opponentName + " gave up!", "You WIN!", JOptionPane.INFORMATION_MESSAGE);
  }

  public void challenge(String opponentName) {
    connection.challenge(opponentName);
  }

  public boolean addOpponent(String opponentToAdd) {
    if (checkIfPlayer(opponentToAdd)) {
      return false;
    } else {
      ListModel<String> oldListModel = opponentList.getModel();
      int oldListModelSize = oldListModel.getSize();
      Vector<String> newOpponentList = new Vector<>(oldListModelSize + 1);

      newOpponentList.add(opponentToAdd);

      boolean notInList = true;

      for (int i = 0; i < oldListModelSize; i++) {
        String opponent = oldListModel.getElementAt(i);
        if (opponent.equals(opponentToAdd))
          notInList = false;
        else
          newOpponentList.add(opponent);
      }

      opponentList.setListData(newOpponentList);
      return notInList;
    }
  }

  public void removeAllOpponents() {
    opponentList.setListData(new Vector<String>());
  }

  public boolean removeOpponent(String opponentToRemove) {
    if (checkIfPlayer(opponentToRemove)) {
      return false;
    } else {
      ListModel<String> oldListModel = opponentList.getModel();
      int oldListModelSize = oldListModel.getSize();
      Vector<String> newOpponentList = new Vector<>(oldListModelSize);

      boolean inList = false;

      for (int i = 0; i < oldListModelSize; i++) {
        String opponent = oldListModel.getElementAt(i);
        if (opponent.equals(opponentToRemove))
          inList = true;
        else
          newOpponentList.add(opponent);
      }

      System.out.println("!!! " + opponentToRemove + " was found in list and removed? " + inList);

      opponentList.setListData(newOpponentList);
      return inList;
    }
  }

  private boolean checkIfPlayer(String opponentName) {
    return playerNameTextField.getText().equals(opponentName);
  }

  private void setSeaGlassLAF() {
    try {
      UIManager.setLookAndFeel("com.seaglasslookandfeel.SeaGlassLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
      e.printStackTrace();
    }
  }

  private void setNimbusLAF() {
    try {
      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
      e.printStackTrace();
    }
  }

  private void toggleConnect() {
    System.out.println("!!! Is in Game? " + (connection != null ? connection.isInGame() : null));
    if (connection == null) {
      connect();
    } else if (connection.isConnected() && !connection.isJoined()) {
      join();
    } else if (!connection.isConnected()) {
      connect();
    } else if (connection.isInGame()) {
      giveup();
    } else {
      disconnect();
    }
  }

  public void giveup() {
    connectButton.setText("Disconnect");
    connection.giveup();
    replaceGameGridWithOpponentList();
    mainPanel.repaint();
  }

  public void disconnect() {
    if (connection != null && connection.isConnected()) {
      System.out.println("Disconnecting!");
      connectButton.setText("Connect");
      connection.disconnect();
      connection = null;
      enableNameAddressPortTextFields();
      chatTextField.setEnabled(false);
      //opponentList.removeAll();
      removeAllOpponents();
      mainPanel.revalidate();
      mainPanel.repaint();
    }
  }

  @SuppressWarnings("unused")
  private void connect() {
    if (connection == null || !connection.isConnected()) {
      System.out.println("Connecting!");
      connectButton.setText("Disconnect");
      try {
        if (false)
          throw new ConnectException();
        connection = new Connection(addressTextField.getText(), Integer.parseInt(portTextField.getText()), MainGUI.this);
        join();
        disableNameAddressPortTextFields();
        chatTextField.setEnabled(true);
      } catch (ConnectException ce) {
        connectButton.setText("Connect");
        System.out.println(ce.getMessage());
      }
    }
  }

  private void join() {
    connectButton.setText("Disconnect");
    connection.setClientState(connection.clientState().joinWith(Join.withName(playerNameTextField.getText())));
  }

  private void enableNameAddressPortTextFields() {
    playerNameTextField.setEnabled(true);
    addressTextField.setEnabled(true);
    portTextField.setEnabled(true);
  }

  private void disableNameAddressPortTextFields() {
    playerNameTextField.setEnabled(false);
    addressTextField.setEnabled(false);
    portTextField.setEnabled(false);
  }

  public void updateGameState(int matchState, int initToken, int[] fieldDescription) {
    GameGUI gameGUI = (GameGUI) contentPanel;
    gameGUI.fieldDescription = fieldDescription;
    int playerIndex = connection.clientState().asInGameState().playerIndex();
    String opponentName = connection.clientState().asInGameState().opponentName();

    if (initToken == playerIndex)
      gameGUI.tokenLabel.setText("Token of Initiative!");
    else
      gameGUI.tokenLabel.setText("");

    if (matchState == 0) {
      gameGUI.enableGameGrid();
      gameGUI.repaint();
    } else if (matchState == playerIndex) {
      // Win
      JOptionPane.showMessageDialog(this, "You won against " + opponentName + "!", "You WIN!", JOptionPane.INFORMATION_MESSAGE);
      replaceGameGridWithOpponentList();
      connection.setClientState(connection.getClientState().backToLobby());
    } else if (matchState == 3) {
      // Remy
      JOptionPane.showMessageDialog(this, "Match against " + opponentName + " ended in a remy!", "REMY!", JOptionPane.INFORMATION_MESSAGE);
      replaceGameGridWithOpponentList();
      connection.setClientState(connection.getClientState().backToLobby());
    } else {
      // Loss
      JOptionPane.showMessageDialog(this, "You lost against " + opponentName + "!", "You LOSE!", JOptionPane.INFORMATION_MESSAGE);
      replaceGameGridWithOpponentList();
      connection.setClientState(connection.getClientState().backToLobby());
    }
  }

  public class LobbyGUI extends JPanel {
    private static final long serialVersionUID = 1L;
    public LobbyGUI() {
      setLayout(null);
      setBounds(12, 67, 300, 260);

      add(opponentList);

      JButton challengeButton = new JButton("Challenge!");
      challengeButton.addActionListener(challengeListener());
      challengeButton.setBounds(0, 235, 300, 25);
      add(challengeButton);
    }
  }

  private void replaceButtonWithCircle(JButton button, Color color) {
    int bx = button.getX();
    int by = button.getY();
    button.setVisible(false);
    contentPanel.remove(button);
    paintCircle(color, bx + 2, by + 2);
  }

  private void paintCircle(Color color, int x, int y) {
    Graphics graphics = contentPanel.getGraphics();
    Graphics2D g2d = (Graphics2D) graphics;
    Double circle = new Ellipse2D.Double(x, y, 20, 20);
    g2d.setColor(color);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.fill(circle);
  }

  public class GameGUI extends JPanel {
    private static final long serialVersionUID = 1L;

    final JButton[][] gameGrid = new JButton[7][6];
    final JLabel tokenLabel = new JLabel("");

    public volatile int[] fieldDescription;

    public GameGUI(String opponentName) {
      setLayout(null);
      setBounds(12, 67, 300, 260);

      for (int x = 24, xi = 0; x <= 246; x += 37, xi += 1) {
        for (int y = 191, yi = 0; y >= 6; y -= 37, yi += 1) {
          JButton button = new JButton();
          button.setBounds(x, y, 25, 25);
          button.addActionListener(cellActionListener(xi, yi));
          add(button);
          gameGrid[xi][yi] = button;
        }
      }

      JLabel gameStatusLabel = new JLabel("You vs. " + opponentName);
      gameStatusLabel.setBounds(8, 230, 138, 25);
      add(gameStatusLabel);

      tokenLabel.setBounds(154, 230, 138, 25);
      add(tokenLabel);

    }

    private ActionListener cellActionListener(final int xi, final int yi) {
      return new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          System.out.println("Cell clicked!");
          connection.send(new MakeMove(xi + 1));
          disableGameGrid();
          contentPanel.repaint();
        }
      };
    }

    @Override
    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (connection != null && connection.isInGame() && fieldDescription != null) {
        int playerIndex = connection.clientState().asInGameState().playerIndex();
        for (int x = 0, i = 0; x < 7; x += 1) {
          for (int y = 0; y < 6; y += 1, i += 1) {
            JButton button = gameGrid[x][y];
            if (fieldDescription[i] == 1) {
              if (playerIndex == 1)
                replaceButtonWithCircle(button, playerColor);
              else
                replaceButtonWithCircle(button, opponentColor);
            } else if (fieldDescription[i] == 2) {
              if (playerIndex == 1)
                replaceButtonWithCircle(button, opponentColor);
              else
                replaceButtonWithCircle(button, playerColor);
            }
          }
        }
      }
    }

    private void disableGameGrid() {
      for (int x = 0; x < 7; x += 1) {
        for (int y = 0; y < 6; y += 1) {
          gameGrid[x][y].setEnabled(false);
        }
      }
      repaint();
    }

    private void enableGameGrid() {
      for (int x = 0; x < 7; x += 1) {
        for (int y = 0; y < 6; y += 1) {
          gameGrid[x][y].setEnabled(true);
        }
      }
      repaint();
    }
  }
}
