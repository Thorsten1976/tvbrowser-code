/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin_oat@yahoo.de)
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
 */

package tvraterplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import util.ui.Localizer;
import util.ui.TabLayout;
import devplugin.Program;
import devplugin.ProgramFieldType;

/**
 * This Dialog shows one Rating
 * 
 * @author bodo tasche
 */
public class DialogRating extends JDialog {
	/** Localizer */
	private static final Localizer _mLocalizer = Localizer.getLocalizerFor(DialogRating.class);
	/** Raiting-Boxes */
	private JComboBox[] _ratings = new JComboBox[6];

	/** Personal Rating */
	private Rating _personalrating;
	/** Overall Rating */
	private Rating _overallrating;
	
	/** Length of the Program */
	private int _length;
	/** Title of teh Program */
	private String _title;
	/** Original Title */
	private String _originaltitle;
	
	/** Rater Plugin */
	private TVRaterPlugin _rater;

	/**
	 * Creates the DialgoRating
	 * 
	 * @param parent ParentFrame
	 * @param rater TVRaterPlugin
	 * @param programtitle the Title of the Program to rate
	 */
	public DialogRating(Frame frame, TVRaterPlugin rater, String programtitle) {
		super(frame, true);
		setTitle(_mLocalizer.msg("title", "View Rating"));

		_rater = rater;
		_overallrating = _rater.getDatabase().getOverallRating(programtitle);
		_personalrating = _rater.getDatabase().getPersonalRating(programtitle);
		
		if (_personalrating == null) {
			_personalrating = new Rating(programtitle);
		}
		
		_length = 90;
		_title = programtitle;
		_originaltitle = null;
		createGUI();
	}


	/**
	 * Creates the DialgoRating
	 * 
	 * @param parent Parent-Frame
	 * @param rater TVRaterPlugin
	 * @param program Program to rate
	 * @param tvraterDB the Database
	 */
	public DialogRating(Frame parent, TVRaterPlugin rater, Program program) {
		super(parent, true);
		setTitle(_mLocalizer.msg("title", "View Rating"));

		_rater = rater;
		_overallrating = _rater.getDatabase().getOverallRating(program);
		_personalrating = rater.getDatabase().getPersonalRating(program);

		if (_personalrating == null) {
			_personalrating = new Rating(program.getTitle());
		}

		_length = program.getLength();
		_title = program.getTitle();
		_originaltitle = program.getTextField(ProgramFieldType.ORIGINAL_TITLE_TYPE);
		createGUI();
	}
	
	/**
	 * Creates the GUI.
	 */
	private void createGUI() {
		JPanel panel = (JPanel) this.getContentPane();

		panel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		JPanel titlePanel = new JPanel(new TabLayout(1));
		titlePanel.setBackground(Color.white);

		JLabel title = new JLabel(_title);
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setFont(new Font("Helvetica", Font.BOLD, 20));
		titlePanel.add(title);

		if (_originaltitle != null) {
			JLabel original = new JLabel("(" + _originaltitle + ")");
			original.setHorizontalAlignment(JLabel.CENTER);
			titlePanel.add(original);
		}

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;

		panel.add(titlePanel, c);

		c.fill = GridBagConstraints.BOTH;
		c.weightx = .5;
		c.weighty = 1.0;
		c.gridwidth = 1;

		panel.add(createRatingPanel(_overallrating), c);

		c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(createVotingPanel(_personalrating), c);


		JPanel buttonpanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 3, 0));

		JButton rate = new JButton(_mLocalizer.msg("rate", "Rate"));

		rate.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    rateWasPressed();
			}
		});

		buttonpanel.add(rate);

		JButton cancel = new JButton(_mLocalizer.msg("cancel", "Cancel"));

		cancel.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				hide();
			}
		});

		buttonpanel.add(cancel);

		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		c.weighty = 0;
		c.insets = new Insets(0, 5, 5, 5);

		panel.add(buttonpanel, c);

		pack();
	}

	private void rateWasPressed(){
	
		int[] values = new int[6];

		for (int i = 0; i < 6; i++) {
			values[i] = _ratings[i].getSelectedIndex();
		}

		_personalrating.setValue(Rating.OVERALL, _ratings[0].getSelectedIndex());
		_personalrating.setValue(Rating.ACTION, _ratings[1].getSelectedIndex());
		_personalrating.setValue(Rating.FUN, _ratings[2].getSelectedIndex());
		_personalrating.setValue(Rating.EROTIC, _ratings[3].getSelectedIndex());
		_personalrating.setValue(Rating.TENSION, _ratings[4].getSelectedIndex());
		_personalrating.setValue(Rating.ENTITLEMENT, _ratings[5].getSelectedIndex());
		
		_rater.getDatabase().setPersonalRating(_personalrating);
		hide();

        if (Integer.parseInt(_rater.getSettings().getProperty("updateIntervall", "0")) == 1) {

            Thread updateThread = new Thread() {

                public void run() {
                    System.out.println("Updater gestartet");
                    Updater up = new Updater(_rater);
                    up.run();
                }
            };
            updateThread.start();
        }

	}
	
	/**
	 * Creates the RatingPanel
	 * 
	 * @param rating Rating to use
	 * @return RatingPanel
	 */
	private JPanel createRatingPanel(Rating rating) {
		JPanel ratingPanel = new JPanel();
		ratingPanel.setBorder(BorderFactory.createTitledBorder(_mLocalizer.msg("overallRating", "Overall Rating")));

		
		if (rating != null) {

			GridBagConstraints labc = new GridBagConstraints();
			labc.weightx = 1;
			labc.weighty = 1;
			labc.fill = GridBagConstraints.BOTH;

			GridBagConstraints c = new GridBagConstraints();
			c.gridwidth = GridBagConstraints.REMAINDER;
			c.weightx = 0;
			c.weighty = 0;
			c.fill = GridBagConstraints.HORIZONTAL;

			ratingPanel.setLayout(new GridBagLayout());
			
			ratingPanel.add(new JLabel(_mLocalizer.msg("overall", "Overall") + ":", JLabel.LEFT), labc);
			ratingPanel.add(createRatingBox( rating.getIntValue(Rating.OVERALL)), c);
			
			ratingPanel.add(new JLabel(_mLocalizer.msg("action", "Action") + ":", JLabel.LEFT), labc);
			ratingPanel.add(createRatingBox(rating.getIntValue(Rating.ACTION)), c);
			
			ratingPanel.add(new JLabel(_mLocalizer.msg("fun", "Fun") + ":", JLabel.LEFT), labc);
			ratingPanel.add(createRatingBox(rating.getIntValue(Rating.FUN)), c);
			
			ratingPanel.add(new JLabel(_mLocalizer.msg("erotic", "Erotic") + ":", JLabel.LEFT), labc);
			ratingPanel.add(createRatingBox(rating.getIntValue(Rating.EROTIC)), c);
			
			ratingPanel.add(new JLabel(_mLocalizer.msg("tension", "Tension") + ":", JLabel.LEFT), labc);
			ratingPanel.add(createRatingBox(rating.getIntValue(Rating.TENSION)), c);
			
			ratingPanel.add(new JLabel(_mLocalizer.msg("entitlement", "Entitlement") + ":", JLabel.LEFT), labc);
			ratingPanel.add(createRatingBox(rating.getIntValue(Rating.ENTITLEMENT)), c);
		} else {
			ratingPanel.setLayout(new BorderLayout());			
			JTextPane pane = new JTextPane();
			pane.setContentType("text/html");
			
			if (_length < TVRaterPlugin.MINLENGTH) {
				pane.setText("<center style='font-family: helvetica'>"+
							_mLocalizer.msg("tooshort1", "Program too short for rating. The minimum lenght is ")
							+ TVRaterPlugin.MINLENGTH + 
							_mLocalizer.msg("tooshort2", " min.<br>This reduces traffic on the server.")
							+"</center>");
			} else {
				pane.setText("<center style='font-family: helvetica'>"+
							_mLocalizer.msg("doesntexist", "Sorry, rating doesn't exist!")
							+"</center>");
			}
			
			pane.setEditable(false);
			ratingPanel.add(pane, BorderLayout.CENTER);
		}


		return ratingPanel;
	}

	/**
	 * Creates a Ratingbox
	 * 
	 * @param name Name of the Rating-Element
	 * @param value Value to show (1-5)
	 * @return JPanel with rating-box
	 */
	private Component createRatingBox(int value) {
		if (value > -1) {
			return new JLabel(RatingIconTextFactory.getStringForRating(value), (Icon)RatingIconTextFactory.getImageIconForRating(value), JLabel.LEFT);
		} else {
			return new JLabel("-");
		}
	}

	/**
	 * Creates a voting-panel 
	 * @param rating Rating to use
	 * @return voting-panel
	 */
	private JPanel createVotingPanel(Rating rating) {
		JPanel voting = new JPanel(new GridBagLayout());
		voting.setBorder(BorderFactory.createTitledBorder(_mLocalizer.msg("yourRating", "Your Rating")));

		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;

		GridBagConstraints labc = new GridBagConstraints();
		labc.weightx = 1;
		labc.weighty = 1;
		labc.fill = GridBagConstraints.BOTH;

		voting.add(new JLabel(_mLocalizer.msg("overall", "Overall") + ":"), labc);
		voting.add(createVotingBox(_personalrating.getIntValue(Rating.OVERALL), 0), c);

		voting.add(new JLabel(_mLocalizer.msg("action", "Action") + ":"), labc);
		voting.add(createVotingBox(_personalrating.getIntValue(Rating.ACTION), 1), c);

		voting.add(new JLabel(_mLocalizer.msg("fun", "Fun") + ":"), labc);
		voting.add(createVotingBox( _personalrating.getIntValue(Rating.FUN), 2), c);

		voting.add(new JLabel(_mLocalizer.msg("erotic", "Erotic") + ":"), labc);
		voting.add(createVotingBox(_personalrating.getIntValue(Rating.EROTIC), 3), c);

		voting.add(new JLabel(_mLocalizer.msg("tension", "Tension") + ":"), labc);
		voting.add(createVotingBox(_personalrating.getIntValue(Rating.TENSION), 4), c);

		voting.add(new JLabel(_mLocalizer.msg("entitlement", "Entitlement") + ":"), labc);
		voting.add(createVotingBox(_personalrating.getIntValue(Rating.ENTITLEMENT), 5), c);

		return voting;
	}

	/**
	 * Creates a voting Box
	 * 
	 * @param name Name of the Rating-Element
	 * @param value Value to select
	 * @param ratingbox number of the Box
	 * @return a Panel with a Voting-Box
	 */
	private Component createVotingBox(int value, int ratingbox) {
		Integer[] values = { new Integer(0), new Integer(1), new Integer(2), new Integer(3), new Integer(4), new Integer(5) };

		JComboBox valuebox = new JComboBox(values);
		
		valuebox.setRenderer(new RatingCellRenderer());
		
		valuebox.setSelectedIndex(value);

		_ratings[ratingbox] = valuebox;

		return valuebox;
	}

}