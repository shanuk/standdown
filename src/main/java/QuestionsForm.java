import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.Properties;

/**
 * Created by sgumirov on 7/7/16.
 */
public class QuestionsForm {

  public static final String Q1 = "q1";
  public static final String Q2 = "q2";
  public static final String Q3 = "q3";
  public static final String TOKEN = "token";

  JTextArea ta1 = new JTextArea(3,40),
            ta2 = new JTextArea(3,40),
      ta3 = new JTextArea(3,40),
      tokenArea = new JTextArea(3,40);
  JButton connect = new JButton("Connect"), token = new JButton("Get token");
  String t1 = "What did you do yesterday?";
  String t2 = "What are you working on today?";
  String t3 = "Is there anything standing in your way?";
  String t4 = "Your Slack auth token";
  private Observer listener;

  public void setListener(Observer list){
    listener = list;
  }

  public QuestionsForm init(Properties config){
    final JFrame frame = new JFrame("Stand down settings");
    JPanel panel = new JPanel();
    frame.getContentPane().add(panel);
    GroupLayout layout = new GroupLayout(panel);
    panel.setLayout(layout);
    layout.setAutoCreateGaps(true);
    layout.setAutoCreateContainerGaps(true);
    JLabel q1 = new JLabel(t1);
    JLabel q2 = new JLabel(t2);
    JLabel q3 = new JLabel(t3);
    JLabel q4 = new JLabel(t4);
    ta1.setText(config.getProperty(Q1));
    ta2.setText(config.getProperty(Q2));
    ta3.setText(config.getProperty(Q3));
    tokenArea.setText(config.getProperty(TOKEN));
    layout.setHorizontalGroup(
        layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(token).addComponent(q1).addComponent(q2).addComponent(q3).addComponent(q4))
        .addGroup(layout.createParallelGroup().addComponent(ta1).addComponent(ta2).addComponent(ta3).addComponent(tokenArea).addComponent(connect))
    );
    layout.setVerticalGroup(
        layout.createSequentialGroup()
        .addGroup(layout.createSequentialGroup().addComponent(token))
        .addGroup(layout.createSequentialGroup().addComponent(q1).addComponent(ta1))
        .addGroup(layout.createSequentialGroup().addComponent(q2).addComponent(ta2))
        .addGroup(layout.createSequentialGroup().addComponent(q3).addComponent(ta3))
        .addGroup(layout.createSequentialGroup().addComponent(q4).addComponent(tokenArea))
        .addGroup(layout.createSequentialGroup().addComponent(connect))
    );
    frame.setPreferredSize(new Dimension(800, 550));
    frame.pack();
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setVisible(true);
    token.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          openWebpage(new URL("https://api.slack.com/docs/oauth-test-tokens?action=issue&sudo=1").toURI());
        } catch (Exception ignore){}
      }
    });

    connect.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (listener != null)
          try {
            listener.update(null, getResults());
            frame.setVisible(false);
          } catch (IllegalStateException e1) {
            JOptionPane.showMessageDialog(frame, e1.getMessage());
          }
      }
    });
    return this;
  }

  private Map<String,String> getResults() throws IllegalStateException {
    HashMap<String,String> res = new HashMap<String,String>();
    res.put(Q1, ta1.getText());
    res.put(Q2, ta2.getText());
    res.put(Q3, ta3.getText());
    res.put(TOKEN, tokenArea.getText());
    if (res.get(Q1).isEmpty()) throw new IllegalStateException("Fill in the whole form please");
    if (res.get(Q2).isEmpty()) throw new IllegalStateException("Fill in the whole form please");
    if (res.get(Q3).isEmpty()) throw new IllegalStateException("Fill in the whole form please");
    if (res.get(TOKEN).isEmpty()) throw new IllegalStateException("Enter in the auth token");
    return res;
  }

  public static void openWebpage(URI uri) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(uri);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
