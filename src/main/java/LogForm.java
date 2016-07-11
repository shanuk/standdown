import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Observer;

/**
 * Created by sgumirov on 7/7/16.
 */
public class LogForm
{
  JButton disconnect = new JButton("Disconnect");
  JTextArea logT = new JTextArea();
  private Observer disconnList;
  private JFrame frame;

  public Component getFrame(){return frame;}

  void init(){
    frame = new JFrame("Standdown Log");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setPreferredSize(new Dimension(800,550));
    frame.getContentPane().add(new JScrollPane(logT), BorderLayout.CENTER);
    frame.getContentPane().add(disconnect, BorderLayout.NORTH);
    disconnect.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (disconnList!=null) disconnList.update(null, null);
        disconnect.setEnabled(false);
      }
    });
    frame.pack();
    frame.setVisible(true);
  }

  SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy:HH:mm ", Locale.ENGLISH);

  public void append(String line)
  {
    logT.insert(df.format(new Date())+line+"\n", 0);
  }
  public void setDisconnectObserver(Observer disconnect)
  {
    this.disconnList = disconnect;
  }
}
