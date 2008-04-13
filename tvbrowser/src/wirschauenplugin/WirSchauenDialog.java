/*
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
 * VCS information:
 *     $Date$
 *   $Author$
 * $Revision$
 */
package wirschauenplugin;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.builder.ButtonBarBuilder;
import devplugin.Program;
import devplugin.ProgramFieldType;
import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.WindowClosingIf;
import util.ui.UiUtilities;

import java.awt.Toolkit;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URLEncoder;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;


public class WirSchauenDialog extends JDialog implements WindowClosingIf {
  /**
   * Localizer
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(WirSchauenDialog.class);

  /**
   * main OMDB url, opened if the movie ID is not yet known
   */
  protected static final String OMDB_MAIN_URL = "http://www.omdb.org";

  /**
   * prefix of any movie url from OMDB, this gets stripped for editing
   */
  private static final String OMDB_MOVIE_URL = "http://www.omdb.org/movie/";

  /**
   * tag for recognizing program descriptions which are already from OMDB, language independent 
   */
  private static final CharSequence OMDB_DESCRIPTION_TAG = " omdb.org";

  private int mButtonpressed = JOptionPane.CANCEL_OPTION;
  private JTextField mOmdb;
  private JComboBox mGenre;
  private JTextArea mDescription;
  private JCheckBox mSubtitle;
  private JCheckBox mOwS;
  private JCheckBox mPremiere;

  private JLabel mCounter;

  private JButton mOpenOmdb;
  
  /**
   * old properties from the server, so we can compare whether anything was changed at all
   */
  private String mOldUrl, mOldGenre, mOldDescription, mOldSubtitle, mOldOws, mOldPremiere;

  private JButton mOk;

  public WirSchauenDialog(JDialog parent, Program program) {
    super(parent, true);
    createGui(program);
  }

  public WirSchauenDialog(JFrame parent, Program program) {
    super(parent, true);
    createGui(program);
  }

  private void createGui(final Program program) {
    setTitle(mLocalizer.msg("title", "WirSchauen.de suggestion"));

    JPanel panel = (JPanel) getContentPane();

    panel.setBorder(Borders.DLU4_BORDER);

    FormLayout layout = new FormLayout("right:pref,3dlu, pref, fill:10dlu:grow, 3dlu, pref",
        "pref," // title
        +"3dlu,"
        +"pref," // episode
        +"3dlu,"
        +"pref," // status
        +"3dlu,"
        +"pref," // omdb URL
        +"3dlu,"
        +"pref," // genre
        +"3dlu,"
        +"fill:50dlu," // description 
        +"3dlu,"
        +"pref," // character count
        +"3dlu,"
        +"pref," // format information
        +"pref," // format information
        +"pref," // format information
        +"fill:pref:grow,"
        +"pref");
    panel.setLayout(layout); // dialog buttons

    CellConstraints cc = new CellConstraints();

    // title
    JLabel titleLabel = new JLabel(mLocalizer.msg("titleLabel", "Title") + ": ");
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
    panel.add(titleLabel, cc.xy(1, 1));
    final JTextField title = new JTextField(program.getTitle());
    title.setEditable(false);
    title.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        title.selectAll();
      }
    });
    panel.add(title, cc.xyw(3, 1, 4));

    // episode
    String text = program.getTextField(ProgramFieldType.EPISODE_TYPE);
    if (text == null || text.length() == 0) {
      text = program.getIntFieldAsString(ProgramFieldType.EPISODE_NUMBER_TYPE);
    }
    if (text != null && text.length() > 0) { 
      JLabel episodeLabel = new JLabel(ProgramFieldType.EPISODE_TYPE.getLocalizedName() + ": ");
      episodeLabel.setFont(episodeLabel.getFont().deriveFont(Font.BOLD));
      panel.add(episodeLabel, cc.xy(1, 3));
      final JTextField episode = new JTextField(text);
      episode.setEditable(false);
      episode.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          episode.selectAll();
        }
      });
      panel.add(episode, cc.xyw(3, 3, 4));
    }
    else {
      // hide the rows for the episode
      layout.setRowSpec(3, new RowSpec("0"));
      layout.setRowSpec(2, new RowSpec("0"));
    }

    // server status message
    String labelText = mLocalizer.msg("countLoading", "Reading data from server...");
    String description = program.getDescription();
    if (description != null && description.contains(OMDB_DESCRIPTION_TAG)) {
      labelText = mLocalizer.msg("countLoadingTagged", "One description available on the server. Updating data...");
    }
    final JLabel countLabel = new JLabel(labelText);
    countLabel.setEnabled(false);
    countLabel.setFont(countLabel.getFont().deriveFont(Font.ITALIC));
    panel.add(countLabel, cc.xyw(3,5,4));

    // URL
    JLabel url = new JLabel(mLocalizer.msg("URL", "URL") + ": ");
    url.setFont(url.getFont().deriveFont(Font.BOLD));
    panel.add(url, cc.xy(1, 7));
    panel.add(new JLabel(OMDB_MOVIE_URL), cc.xy(3, 7));

    NumberFormat integerFormat = NumberFormat.getIntegerInstance();
    integerFormat.setGroupingUsed(false);
    integerFormat.setParseIntegerOnly(true);
//    mOmdb = new JFormattedTextField(integerFormat);
    mOmdb = new JTextField();
    mOmdb.setToolTipText(mLocalizer.msg("tooltip.omdbId","Numerical ID of the program"));
    panel.add(mOmdb, cc.xy(4, 7));

    mOmdb.setDocument(new PlainDocument() {
      @Override
      public void insertString(int offset, String input, AttributeSet a) throws BadLocationException {
        String filtered = input.replaceAll("\\D", "");
        if (!filtered.equals(input)) {
          Toolkit.getDefaultToolkit().beep();
        }
        super.insertString(offset, filtered, a);
      }
    });
    
    mOmdb.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        updateOkButton();
      }

      public void insertUpdate(DocumentEvent e) {
        updateOkButton();
      }

      public void removeUpdate(DocumentEvent e) {
        updateOkButton();
      }});

    mOpenOmdb = new JButton(WirSchauenPlugin.getInstance().createImageIcon("apps", "internet-web-browser", 16));
    mOpenOmdb.setToolTipText(mLocalizer.msg("tooltip.openURL","Open URL"));
    panel.add(mOpenOmdb, cc.xy(6, 7));
    
    mOpenOmdb.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        String movieId = mOmdb.getText().trim();
        if (movieId.length() == 0) {
          Launch.openURL(OMDB_MAIN_URL);
        }
        else {
          Launch.openURL(OMDB_MOVIE_URL + movieId);
        }
      }});

    // genre
    JLabel genre = new JLabel(mLocalizer.msg("genre","Genre")+": ");
    genre.setFont(genre.getFont().deriveFont(Font.BOLD));

    panel.add(genre, cc.xy(1, 9));
    String[] genres = mLocalizer.msg("genreDefaults","Action,Adventure,Animation").split(",");
    Arrays.sort(genres);
    mGenre = new JComboBox(genres);
    mGenre.setEditable(true);
    mGenre.setMaximumRowCount(8);
    mGenre.setSelectedIndex(-1);
    //TODO: Use util.ui class after 2.7 release
    new AutoCompletion(mGenre, true);

    panel.add(mGenre, cc.xyw(3, 9, 4));

    // description
    JLabel descLabel = new JLabel(mLocalizer.msg("text","Text")+": ");
    descLabel.setVerticalAlignment(JLabel.TOP);
    descLabel.setFont(descLabel.getFont().deriveFont(Font.BOLD));

    panel.add(descLabel, cc.xy(1, 11));

    mDescription = new JTextArea();
    mDescription.setDocument(new PlainDocument() {
      @Override
      public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
        str = str.replaceAll("\t", "");
        if (getLength() + str.length() > 200) {
          Toolkit.getDefaultToolkit().beep();
        } else {
          super.insertString(offs, str, a);
        }
      }
    });
    mDescription.setLineWrap(true);
    mDescription.setWrapStyleWord(true);
    mDescription.getDocument().addDocumentListener(new DocumentListener() {
      private void updateRemaining() {
        int remaining = 200 - mDescription.getDocument().getLength();
        mCounter.setText(mLocalizer.msg("remaining", "({0} characters remaining)", remaining));
        updateOkButton();
      }

      public void changedUpdate(DocumentEvent e) {
        updateRemaining();
      }

      public void insertUpdate(DocumentEvent e) {
        updateRemaining();
      }

      public void removeUpdate(DocumentEvent e) {
        updateRemaining();
      }});

    panel.add(new JScrollPane(mDescription, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), cc.xyw(3, 11, 4));

    // character count
    mCounter = new JLabel(mLocalizer.msg("maxChars","(max. 200 characters)"));
    panel.add(mCounter, cc.xyw(3, 13, 4));

    // format information
    JLabel format = new JLabel(mLocalizer.msg("format","Format")+": ");
    format.setFont(format.getFont().deriveFont(Font.BOLD));
    panel.add(format, cc.xy(1, 15));

    
    mSubtitle = new JCheckBox(mLocalizer.msg("subtitle", "Untertitel"));
    mOwS = new JCheckBox(mLocalizer.msg("OwS", "Original with subtitle"));
    mPremiere = new JCheckBox(mLocalizer.msg("premiere", "Television premiere"));

    panel.add(mSubtitle, cc.xyw(3, 15, 2));
    panel.add(mOwS, cc.xyw(3, 16, 2));
    panel.add(mPremiere, cc.xyw(3, 17, 2));

    mOk = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    mOk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ok();
      }
    });

    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    getRootPane().setDefaultButton(mOk);

    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();
    builder.addGriddedButtons(new JButton[]{mOk, cancel});

    panel.add(builder.getPanel(), cc.xyw(1,19,6));

    // TODO: change to 2.7 size storage mechanism after 2.7 release
    setSize(Sizes.dialogUnitXAsPixel(300, this),
            Sizes.dialogUnitYAsPixel(240, this));

    UiUtilities.registerForClosing(this);

    mOk.setEnabled(false);
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mOmdb.requestFocusInWindow();
        int count = getCountForProgram(program);

        if (count == -1) {
          countLabel.setText(mLocalizer.msg("countError", "Could not load data from server..."));
        } else if (count == 0) {
          countLabel.setText(mLocalizer.msg("countNone", "No description available on the server."));
        } else if (count == 1) {
          countLabel.setText(mLocalizer.msg("countOne", "One description available on the server. But you could add a better one."));
        }

        countLabel.setFont(countLabel.getFont().deriveFont(Font.PLAIN));
      }
    });
  }

  protected void updateOkButton() {
    mOk.setEnabled((getDescription().length() > 0) || (getUrl().length() > 0));
  }

  private void ok() {
    setVisible(false);
    mButtonpressed = JOptionPane.OK_OPTION;
  }

  public void close() {
    setVisible(false);
  }

  public int getButtonPressed() {
    return mButtonpressed;
  }

  public String getUrl() {
    String text = mOmdb.getText().trim();
    if (text.length() > 0) {
      return OMDB_MOVIE_URL + text;
    }
    return "";
  }

  public String getGenre() {
    Object selectedItem = mGenre.getSelectedItem();
    if (selectedItem == null) {
      return "";
    }
    return selectedItem.toString().trim();
  }

  public String getDescription() {
    return mDescription.getText().trim();
  }

  public String getPremiere() {
    return mPremiere.isSelected() ? "true": "false";
  }

  public String getOws() {
    return mOwS.isSelected()? "true":"false";
  }

  public String getSubtitle() {
    return mSubtitle.isSelected()? "true":"false";
  }

  public int getCountForProgram(Program program) {
    // reset all old values
    mOldUrl = "";
    mOldGenre = "";
    mOldDescription = "";
    mOldSubtitle = "false";
    mOldOws = "false";
    mOldPremiere = "false";
    
    // now ask the server
    int count = -1;
    StringBuilder url = new StringBuilder();
    try {
      url = url.append("channel=").append(URLEncoder.encode(program.getChannel().getId(), "UTF-8"));
      url = url.append("&day=").append(program.getDate().getDayOfMonth());
      url = url.append("&month=").append(program.getDate().getMonth());
      url = url.append("&year=").append(program.getDate().getYear());
      url = url.append("&hour=").append(program.getHours());
      url = url.append("&minute=").append(program.getMinutes());
      url = url.append("&length=").append(program.getLength());
      url = url.append("&title=").append(URLEncoder.encode(program.getTitle(), "UTF-8"));

      URL scriptUrl = new URL(WirSchauenPlugin.BASE_URL + "findDescription/?"+ url);

      WirSchauenHandler handler = new WirSchauenHandler();
      SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
      saxParser.parse(scriptUrl.openStream(), handler);

      if (handler.getData().get("desc_id") != null) {
        count = 1;

        HashMap<String, String> data = handler.getData();

        mOldUrl = getServerValue(data, "url");
        String urlText = mOldUrl;
        if (urlText.startsWith(OMDB_MOVIE_URL)) {
          urlText = urlText.substring(OMDB_MOVIE_URL.length()).trim();
        }
        mOmdb.setText(urlText);

        // if an URL is available, make the browser button the default control
        if (urlText.length() > 0) {
          mOpenOmdb.requestFocusInWindow();
        }

        mOldDescription = getServerValue(data, "desc");
        mDescription.setText(mOldDescription);

        mOldGenre = getServerValue(data, "genre");
        mGenre.setSelectedItem(mOldGenre);
        
        mOldPremiere = getServerValue(data, "premiere");
        mPremiere.setSelected(mOldPremiere.equalsIgnoreCase("true"));
        
        mOldSubtitle = getServerValue(data, "subtitle");
        mSubtitle.setSelected(mOldSubtitle.equalsIgnoreCase("true"));

        mOldOws = getServerValue(data, "omu");
        mOwS.setSelected(mOldOws.equalsIgnoreCase("true"));
      } else {
        count = 0;
      }
    } catch (NumberFormatException e) {
      e.printStackTrace();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return count;
  }

  private String getServerValue(HashMap<String, String> data, String key) {
    String text = data.get(key);
    if (text != null) {
      text = text.trim();
    }
    else {
      text = "";
    }
    return text;
  }
  
  protected boolean hasChanged() {
    return !mOldUrl.equals(getUrl()) || !mOldGenre.equals(getGenre())
        || !mOldDescription.equals(getDescription())
        || !mOldSubtitle.equalsIgnoreCase(getSubtitle())
        || !mOldOws.equalsIgnoreCase(getOws())
        || !mOldPremiere.equalsIgnoreCase(getPremiere());
  }
}
