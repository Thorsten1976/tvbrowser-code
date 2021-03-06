/*
 * TV-Pearl improvement by René Mach
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * SVN information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package tvpearlplugin;

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import compat.BordersCompat;
import devplugin.Program;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.html.HTMLTextHelper;

public class PearlCreationJPanel extends JPanel {
  static final Localizer mLocalizer = Localizer.getLocalizerFor(PearlCreationJPanel.class);
  
  private PearlCreationTableModel mCreationTableModel;
  private JTable mTable;
  
  private TVPearlSettings mSettings;
  
  public PearlCreationJPanel(PearlCreationTableModel model, TVPearlSettings settings) {
    mCreationTableModel = model;
    mSettings = settings;
    createGUI();
  }
    
  private void createGUI() {try{
    setOpaque(false);
    
    PanelBuilder pb = new PanelBuilder(new FormLayout("default,default:grow,default","fill:default:grow,3dlu,default"),this);
    pb.getPanel().setBorder(BordersCompat.getDialogBorder());
    
    PearlCreationTableCellRenderer renderer = new PearlCreationTableCellRenderer();
    
    mTable = new JTable(mCreationTableModel);
    updateFormatingEditor(TVPearlPlugin.getInstance().getSelectedPluginProgramFormatings());
    updateCommentEditor(TVPearlPlugin.getSettings().getCommentValues());
    
    mTable.getTableHeader().setReorderingAllowed(false);
    
    mTable.getColumnModel().getColumn(0).setMinWidth(200);
    mTable.getColumnModel().getColumn(0).setPreferredWidth(300);
    mTable.getColumnModel().getColumn(0).setMaxWidth(300);
    
    mTable.getColumnModel().getColumn(1).setMinWidth(200);
    
    mTable.getColumnModel().getColumn(2).setMinWidth(200);
    mTable.getColumnModel().getColumn(2).setMaxWidth(200);
    mTable.getColumnModel().getColumn(0).setCellRenderer(renderer);
    mTable.getColumnModel().getColumn(2).setCellRenderer(renderer);
    
    JScrollPane scroll = new JScrollPane(mTable);
    scroll.getViewport().setBackground(UIManager.getDefaults().getColor("List.background"));
    
    final JButton delete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    final JButton send = new JButton(mLocalizer.msg("post", "Post in message board"),TVBrowserIcons.webBrowser(TVBrowserIcons.SIZE_SMALL));
    
    delete.setEnabled(mCreationTableModel.getRowCount() > 0);
    send.setEnabled(mCreationTableModel.getRowCount() > 0);
    
    delete.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mCreationTableModel.clear();
      }
    });
    
    send.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        showSendDialog();
      }
    });
    
    mCreationTableModel.addTableModelListener(new TableModelListener() {
      @Override
      public void tableChanged(TableModelEvent e) {
        delete.setEnabled(mCreationTableModel.getRowCount() > 0);
        send.setEnabled(mCreationTableModel.getRowCount() > 0);
      }
    });
    
    pb.add(scroll, CC.xyw(1, 1, 3));
    pb.add(delete, CC.xy(1, 3));
    pb.add(send, CC.xy(3, 3));}catch(Throwable t) {t.printStackTrace();}
  }
  
  private void showSendDialog() {
    final JDialog dialog = new JDialog(UiUtilities.getBestDialogParent(this), mLocalizer.msg("post", "Post in message board"), ModalityType.APPLICATION_MODAL);
    
    FormLayout layout = new FormLayout("default,3dlu,default:grow","default,3dlu,default,5dlu,default");
    
    PanelBuilder pb = new PanelBuilder(layout);
    pb.getPanel().setBorder(BordersCompat.getDialogBorder());
    
    dialog.setContentPane(pb.getPanel());
    
    final JTextField userName = new JTextField(mSettings.getForumUserName());
    userName.setPreferredSize(new Dimension(300,userName.getPreferredSize().height));
    
    final JPasswordField password = new JPasswordField(mSettings.getForumPassword());
    
    pb.addLabel(TVPearlPluginSettingsTab.mLocalizer.msg("forumUser","Message board user name:"), CC.xy(1, 1));
    pb.add(userName, CC.xy(3, 1));
    
    pb.addLabel(TVPearlPluginSettingsTab.mLocalizer.msg("forumPassword","Message board user password:"), CC.xy(1, 3));
    pb.add(password, CC.xy(3, 3));
    
    final JButton send = new JButton(mLocalizer.msg("post", "Post in message board"),TVBrowserIcons.webBrowser(TVBrowserIcons.SIZE_SMALL));
    send.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(mTable.isEditing()) {
          mTable.getCellEditor().stopCellEditing();
        }
        
        final StringBuilder message = new StringBuilder();
        final ParamParser parser = new ParamParser();
        
        ArrayList<String> commentList = new ArrayList<String>();
        
        for(int row = 0; row < mCreationTableModel.getRowCount(); row++) {
          final Program program = (Program)mCreationTableModel.getValueAt(row, 0);
          final String comment = ((String)mCreationTableModel.getValueAt(row, 1)).trim();
          
          if(comment.trim().length() > 0 && !commentList.contains(comment)) {
            commentList.add(comment);
          }
          
          final AbstractPluginProgramFormating formating = (AbstractPluginProgramFormating)mCreationTableModel.getValueAt(row, 2);
        
          if(!program.isExpired() && !program.isOnAir()) {
            final String programText = parser.analyse(formating.getContentValue(), program);
            if (programText != null) {
              message.append(programText.trim());
              
              if(comment.length() > 0) {
                message.append("\n");
                message.append(comment);
              }
              
              message.append("\n\n");
            }
          }
        }
        
        String[] currentComments = TVPearlPlugin.getSettings().getCommentValues();
        
        for(String comment : currentComments) {
          if(!commentList.contains(comment)) {
            commentList.add(comment);
          }
        }
        
        ForenAnswer answer = null;
        
        if(message.length() > 0) {
          answer = postPearls(message.toString(), userName.getText().trim(), new String(password.getPassword()).trim());
                    
          TVPearlPlugin.getSettings().setCommentValues(commentList.toArray(new String[commentList.size()]));
          updateCommentEditor(TVPearlPlugin.getSettings().getCommentValues());
        }
        
        if(answer != null && answer.wasSuccessfull()) {
          mSettings.setForumUserName(userName.getText().trim());
          JOptionPane.showMessageDialog(dialog, mLocalizer.msg("success", "TV pearls were posted successfully."));
          
          mCreationTableModel.clear();
          TVPearlPlugin.getInstance().run();
        }
        else if(answer != null) {
          JOptionPane.showMessageDialog(dialog, answer.getAnswer(), Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
        }
        else {
          JOptionPane.showMessageDialog(dialog, mLocalizer.msg("noSuccess", "TV pearls could not be posted.\nPlease check your user name and password and Internet connection."), Localizer.getLocalization(Localizer.I18N_ERROR), JOptionPane.ERROR_MESSAGE);
        }
        
        dialog.dispose();
      }
    });

    dialog.getRootPane().setDefaultButton(send);
    
    CaretListener listener = new CaretListener() {
      @Override
      public void caretUpdate(CaretEvent e) {
        send.setEnabled(userName.getText().trim().length() > 0 && new String(password.getPassword()).trim().length() > 0);
      }
    };
    
    userName.addCaretListener(listener);
    password.addCaretListener(listener);
    
    send.setEnabled(userName.getText().trim().length() > 0 && new String(password.getPassword()).trim().length() > 0);
    
    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        dialog.dispose();
      }
    });
    
    ButtonBarBuilder2 buttons = new ButtonBarBuilder2();
    
    buttons.addGlue();
    buttons.addButton(cancel);
    buttons.addUnrelatedGap();
    buttons.addButton(send);
    buttons.addGlue();
    
    pb.add(buttons.getPanel(), CC.xyw(1,5,3));
    
    dialog.pack();
        
    dialog.setLocationRelativeTo(this);
    dialog.setVisible(true);
  }
  
  private void updateCommentEditor(String[] values) {
    if(values != null) {
      final JComboBox comboBox = new JComboBox();
      comboBox.setEditable(true);
      
      for(final String formating : values) {
        if(comboBox != null && formating != null) {
          try {
            comboBox.addItem(formating);
          }catch(Exception e) {e.printStackTrace();}
        }
      }
      
      if(comboBox != null) {
        mTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboBox));
      }
    }
  }
  
  public synchronized void updateFormatingEditor(AbstractPluginProgramFormating[] values) {
    if(values != null) {
      final JComboBox comboBox = new JComboBox();
      
      for(final AbstractPluginProgramFormating formating : values) {
        if(comboBox != null && formating != null && formating.toString() != null && formating.getContentValue() != null && formating.getEncodingValue() != null && formating.getName() != null) {
          try {
            comboBox.addItem(formating);
          }catch(Exception e) {}
        }
      }
      
      if(comboBox != null) {
        mTable.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(comboBox));
      }
    }
  }
  

  private ForenAnswer postPearls(String text, String userName, String password) {
    String errorUnknown = mLocalizer.msg("errorUnknown", "Reason unknown.");
    String errrorInfo = mLocalizer.msg("errorInfo", "\n\nPlease check your user name and password and Internet connection.");
    
    ForenAnswer answer = new ForenAnswer(false,mLocalizer.msg("noSuccess","TV pearls could not be posted:\n\n'{0}'{1}",errorUnknown,errrorInfo));
    Charset utf8 = Charset.forName("UTF-8");
    
    try {
      HttpGet loginForm = new HttpGet("https://hilfe.tvbrowser.org/ucp.php?mode=login");
      
      CloseableHttpClient client = HttpClients.createDefault();
     
      CloseableHttpResponse response = client.execute(loginForm);
      
      HttpEntity result = response.getEntity();

      String content = new String(EntityUtils.toString(result).getBytes(utf8),utf8);
      
      int loginIndex = content.indexOf("<form action=\"./ucp.php?mode=login");
      
      content = content.substring(loginIndex,content.indexOf("</form>",loginIndex));
      
      Pattern getInputs = Pattern.compile("<input.*?name=\"(.*?)\".*?(?:value=\"(.*?)\"|/>)");
      Matcher m = getInputs.matcher(content);
      
      int lastPos = 0;
      
      ArrayList<BasicNameValuePair> postValues = new ArrayList<BasicNameValuePair>();
      
      String SID = null;
      String logoutLink = null;
      
      while(m.find(lastPos)) {
        if(m.groupCount() == 2) {
          String name = m.group(1);
          String value = m.group(2);
          
          if(name.equals("password")) {
            value = new String(password.getBytes(utf8),utf8);
          }
          else if(name.equals("username")) {
            value = new String(userName.getBytes(utf8),utf8);
          }
          else if(name.equals("sid")) {
            SID = new String(value.getBytes(utf8),utf8);
          }
          
          if(value == null) {
            value = "";
          }
          
          postValues.add(new BasicNameValuePair(name, value));
        }
        
        lastPos = m.end();
      }
      
      EntityUtils.consume(result);
      response.close();

      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(postValues, Consts.UTF_8);
      
      HttpPost post = new HttpPost("https://hilfe.tvbrowser.org/ucp.php?mode=login&sid=" + SID);
      post.setEntity(entity);
      
      response = client.execute(post);
      
      if(response.getStatusLine().getStatusCode() == 302) {
        String location = response.getFirstHeader("Location").getValue();
        if(location!=null) {
          HttpGet get = new HttpGet(location);
          response.close();
          
          response = client.execute(get);
        }
      }        
      
      if(response.getStatusLine().getStatusCode() == 200) {
        result = response.getEntity();
        
        content = new String(EntityUtils.toString(result).getBytes(utf8),utf8);
        
        Pattern logout = Pattern.compile("<a href=\"(./ucp.php\\?mode=logout.*?)\"");
        m = logout.matcher(content);
        
        m.find();
        
        logoutLink = m.group(1);
        
        if(logoutLink.startsWith("./")) {
          logoutLink = logoutLink.substring(2, logoutLink.length());
        }
      
        logoutLink = StringEscapeUtils.unescapeHtml4(logoutLink);
        
        EntityUtils.consume(result);
        
        response.close();
        
        HttpGet testPost = new HttpGet("https://hilfe.tvbrowser.org/posting.php?mode=reply&f=27&t=1470");
        response = client.execute(testPost);
        result = response.getEntity();
        
        content = new String(EntityUtils.toString(result).getBytes(utf8),utf8);
        
        
        int formIndex = content.indexOf("form id=\"postform\"");
        
        content = content.substring(formIndex,content.indexOf("</form",formIndex));
        
        Pattern url = Pattern.compile("id=\"postform\".*?action=\"(.*?)\"");
        Matcher urlMatcher = url.matcher(content);
        
        urlMatcher.find();
        
        String postAction = urlMatcher.group(1);
        
        if(postAction.startsWith("./")) {
            postAction=postAction.substring(2, postAction.length());
        }
        
        postAction = StringEscapeUtils.unescapeHtml4(postAction);
                    
        MultipartEntityBuilder test = MultipartEntityBuilder.create();
        //test.setCharset(Charset.forName("UTF-8"));
        
        getInputs = Pattern.compile("<input.*?type=\"(.*?)\".*?name=\"(.*?)\".*?(?:value=\"(.*?)\"|/>)");
        
        m = getInputs.matcher(content);
        
        lastPos = 0;
        long topicPostID = 0;
        
        System.out.println("Formular entries: ");
        System.out.print("      ");
        System.out.println("message = " + text);
        
        test.addTextBody("message", new String(text.getBytes(utf8),utf8), ContentType.create("text/plain", Charset.forName("UTF-8")));
        
        while(m.find(lastPos)) {
          if(m.group(1).equals("text") || m.group(1).equals("hidden") || (m.group(1).equals("submit") && m.group(2).equals("post"))) {
            String name = m.group(2);
            String value = m.group(3);
            
            if(name.equals("lastclick")) {
              value = String.valueOf(Integer.parseInt(value));
            }
            
            if(name.equals("topic_cur_post_id")){
              topicPostID = Integer.parseInt(value);
            }
            
            if(value == null) {
              value = "";
            }
            
            System.out.print("      ");
            System.out.println(name + " = " + value);

            test.addTextBody(name,new String(value.getBytes(utf8),utf8),ContentType.create("text/plain", Charset.forName("UTF-8")));
          }
          else {
            System.out.println(" NOT USED INPUT " + m.group(2) + " = " + m.group(3));
          }
          
          lastPos = m.end();
        }
        
        EntityUtils.consume(result);
        
        HttpEntity form = test.build();
        
        String postURL = "https://hilfe.tvbrowser.org/" + postAction;
        
        post = new HttpPost(postURL);
        post.setHeader("Referer", postURL);
        post.setEntity(form);
        
        Thread t = new Thread("WAIT_BEFORE_POST") {
          public void run() {
            try {
              sleep(2000);
            } catch (InterruptedException e) {}
          }
        };
        
        t.start();
        
        try {
          t.join();
        }catch(InterruptedException e) {}
        
        response = client.execute(post);
        
        if(response.getStatusLine().getStatusCode() == 302) {
          String location = response.getFirstHeader("Location").getValue();
          if(location!=null) {
            HttpPost post2 = new HttpPost(location);
            post2.addHeader("Referer",postURL);
            post2.setEntity(form);
            response.close();           
    
            response = client.execute(post2);            
          }
        }
                
        result = response.getEntity();
        
        content = new String(EntityUtils.toString(result).getBytes(utf8),utf8);
        
        Pattern checkError = Pattern.compile("<p class=\"error\">(.*?)</p>");
        m = checkError.matcher(content);
        
        if(m.find()) {
          answer = new ForenAnswer(false, mLocalizer.msg("noSuccess","TV pearls could not be posted:\n\n'{0}'{1}",HTMLTextHelper.convertHtmlToText(m.group(1)),""));
        }
        else {
          final Matcher matcher = TVPGrabber.getPatternContent().matcher(content);

          while (matcher.find())
          {
            final String author = matcher.group(4).trim();
            final String postID = matcher.group(2).trim();
            int curPostID = Integer.parseInt(postID);
            if(author.equals(userName) && (curPostID > topicPostID)){
              answer = new ForenAnswer(true,mLocalizer.msg("success", "TV pearls were posted successfully."));
              break;
            }
          }
        }
                
        EntityUtils.consume(result);
        response.close();
      }
      
      HttpGet logout = new HttpGet("https://hilfe.tvbrowser.org/"+logoutLink);
      response = client.execute(logout);
      
      result = response.getEntity();
      
      EntityUtils.consume(result);
      
      response.close();
      
      client.close();
    } catch (Throwable e) {
      e.printStackTrace();
    }
    
    return answer;
  }
  
  private class ForenAnswer {
    boolean mSuccess;
    String mAnswer;
    
    public ForenAnswer(boolean success, String answer) {
      mSuccess = success;
      mAnswer = answer;
    }
    
    public String getAnswer() {
      return mAnswer;
    }
    
    public boolean wasSuccessfull() {
      return mSuccess;
    }
  }
  
  @Override
  public void requestFocus() {
    mTable.requestFocus();
  }
}
