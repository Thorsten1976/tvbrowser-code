using System;
using System.Collections.Generic;
using System.Collections;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Windows.Forms;
using System.IO;
using System.Net;
using System.Net.Sockets;

namespace TVBrowserMini
{
    public partial class Configuration : Form
    {
        private TVBrowserControll con;
        private String loadedLanguage = "";
        private Hashtable languages;

        public Configuration(TVBrowserControll con)
        {
            this.con = con;
            InitializeComponent();

            string name = Dns.GetHostName();
            IPHostEntry e = Dns.Resolve(name);

            this.labelDNSValue.Text = name;
            this.labelIPValue.Text = e.AddressList[0].ToString();


            this.initVideoMode();
            this.refreshLanguage();
            this.fillComboBoxHours();
            this.fillComboLanguages();
            this.MinimizeBox = false;
            this.load();
        }

        private void initVideoMode()
        {
            this.tabControlConfiguration.Width = this.Width;
            this.tabControlConfiguration.Height = this.Height;
            this.tabGeneral.Width = this.Width;
            this.tabGeneral.Height = this.Height;
        }

        protected override void OnResize(EventArgs e)
        {
            this.initVideoMode();
            base.OnResize(e);
        }

        
        private void buttonBrowse_Click(object sender, EventArgs e)
        {
            this.openFileDialogTVData.InitialDirectory = this.con.getSystemPath();
            this.openFileDialogTVData.ShowDialog();
            this.textBoxPathDB.Text = this.openFileDialogTVData.ToString();
        }

        private void bBrowseTVData_Click(object sender, EventArgs e)
        {
            this.openFileDialogTVData.FileName = "tvdata.tvd";
            this.openFileDialogTVData.Filter = "TVData Files (*.tvd)|*.tvd";
            this.openFileDialogTVData.InitialDirectory = this.con.getSystemPath();
            if (this.openFileDialogTVData.ShowDialog() == DialogResult.OK)
            {
                this.textBoxPathDB.Text = this.openFileDialogTVData.FileName;
            }
        }

        public void save()
        {
            try
            {
                StreamReader re = File.OpenText(this.con.getSystemPath() + "\\conf.csv");
                String input = null;
                String copy = "";
                while ((input = re.ReadLine()) != null)
                {
                    if (input.StartsWith("[$dbPath]"))
                    {
                        input = "[$dbPath]" + this.textBoxPathDB.Text;
                        copy += input + "\r\n";
                    }
                    else if (input.StartsWith("[$reload]"))
                    {
                        input = "[$reload]" + this.comboBoxReload.SelectedIndex;
                        copy += input + "\r\n";
                    }
                    else if (input.StartsWith("[$reminderWav]"))
                    {
                        input = "[$reminderWav]" + this.textBoxPathReminderSound.Text;
                        copy += input + "\r\n";
                    }
                    else if (input.StartsWith("[$reminderMinutes]"))
                    {
                        try
                        {
                            int test = Int32.Parse(this.textBoxMinutes.Text);
                        }
                        catch
                        {
                            this.textBoxMinutes.Text = "5";
                        }
                        input = "[$reminderMinutes]" + this.textBoxMinutes.Text;
                        copy += input + "\r\n";
                    }
                    else if (input.StartsWith("[$reminderPopup]"))
                    {
                        input = "[$reminderPopup]" + this.checkBoxPopupReminders.Checked;
                        copy += input + "\r\n";
                    }
                    else if (input.StartsWith("[$reminderPlaySound]"))
                    {
                        input = "[$reminderPlaySound]" + this.checkBoxSoundReminder.Checked;
                        copy += input + "\r\n";
                    }
                    else if (input.StartsWith("[$language]"))
                    {
                        input = "[$language]" + this.languages[this.comboBoxLanguages.SelectedItem.ToString()].ToString();
                        copy += input + "\r\n";
                    }
                    else if (input.StartsWith("[$daystartsat]"))
                    {
                        input = "[$daystartsat]" + this.comboBoxDayStarts.SelectedIndex;
                        copy += input + "\r\n";
                    }
                    else if (input.StartsWith("[$dayendsat]"))
                    {
                        input = "[$dayendsat]" + this.comboBoxDayEnds.SelectedIndex;
                        copy += input + "\r\n";
                    }
                    else if (input.StartsWith("[$port]"))
                    {
                        input = "[$port]" + this.textBoxPort.Text;
                        copy += input + "\r\n";
                    }
                    else if (input.StartsWith("[$trans]"))
                    {
                        input = "[$trans]" + this.checkBoxTrans.Checked;
                        copy += input + "\r\n";
                    }
                }
                re.Close();
                FileInfo t = new FileInfo(this.con.getSystemPath() + "\\conf.csv");
                try
                {
                    StreamWriter writer = t.CreateText();
                    writer.Write(copy);
                    writer.Close();
                    this.Close();
                }
                catch
                {
                    //nothing
                }
                if (this.con.hasDB())
                    this.con.closeDBConnect();
                this.con.loadConf();
                if (this.con.checkDBExists())
                {
                    this.con.sqlinit();
                    this.con.getMainform().fillChannels();
                    this.con.getMainform().setComboBoxChannelIndex(0);
                }
                if (!this.loadedLanguage.Equals(this.languages[this.comboBoxLanguages.SelectedItem.ToString()]))
                {
                    DialogResult result = MessageBox.Show(this.con.getLanguageElement("Configuration.Startnew", "You have changed the language. You have to restart the programm to adopt changes. \r\nClose PocketTvBrowser?"), this.con.getLanguageElement("Configuration.Warning", "Warning!"), MessageBoxButtons.OKCancel, MessageBoxIcon.Question, MessageBoxDefaultButton.Button2);
                    if (result == DialogResult.OK)
                    {
                        Application.Exit();
                    }
                }
            }
            catch
            {
                MessageBox.Show(this.con.getLanguageElement("Configuration.CantSave", "PocketTVBrowser wasn't able to save the configuration"), this.con.getLanguageElement("Configuration.Warning", "Warning!"));
            }
        }

        public void load()
        {
            try
            {
                StreamReader re = File.OpenText(this.con.getSystemPath() + "\\conf.csv");
                String input = null;
                while ((input = re.ReadLine()) != null)
                {
                    try
                    {
                        if (input.StartsWith("#"))
                        {
                        }
                        else if (input.StartsWith("[$dbPath]"))
                        {
                            input = input.Remove(0, 9);
                            if (input == "")
                                input = "tvdata.tvd";
                            this.checkBoxTrans.Enabled = true;
                            this.textBoxPathDB.Text = input;
                        }
                        else if (input.StartsWith("[$reload]"))
                        {
                            input = input.Remove(0, 9);
                            this.comboBoxReload.SelectedIndex = Int32.Parse(input);
                        }
                        else if (input.StartsWith("[$reminderWav]"))
                        {
                            input = input.Remove(0, 14);
                            this.textBoxPathReminderSound.Text = input;
                        }
                        else if (input.StartsWith("[$reminderMinutes]"))
                        {
                            input = input.Remove(0, 18);
                            this.textBoxMinutes.Text = input;
                        }
                        else if (input.StartsWith("[$reminderPopup]"))
                        {
                            input = input.Remove(0, 16);
                            if (input == "True")
                            {
                                this.checkBoxPopupReminders.Checked = true;
                            }
                            else if (input == "False")
                            {
                                this.checkBoxPopupReminders.Checked = false;
                            }
                        }
                        else if (input.StartsWith("[$reminderPlaySound]"))
                        {
                            input = input.Remove(0, 20);
                            if (input == "True")
                            {
                                this.checkBoxSoundReminder.Checked = true;
                            }
                            else if (input == "False")
                            {
                                this.checkBoxSoundReminder.Checked = false;
                            }
                        }
                        else if (input.StartsWith("[$language]"))
                        {
                            input = input.Remove(0, 11);
                            this.comboBoxLanguages.Text = this.con.getLanguageElement("Filesystem.Language." + input, input);
                            this.loadedLanguage = input;
                        }
                        else if (input.StartsWith("[$daystartsat]"))
                        {
                            input = input.Remove(0, 14);
                            this.comboBoxDayStarts.SelectedIndex = Int32.Parse(input);
                        }
                        else if (input.StartsWith("[$dayendsat]"))
                        {
                            input = input.Remove(0, 12);
                            this.comboBoxDayEnds.SelectedIndex = Int32.Parse(input);
                        }
                        else if (input.StartsWith("[$port]"))
                        {
                            input = input.Remove(0, 7);
                            this.textBoxPort.Text = input;
                        }
                        else if (input.StartsWith("[$trans]"))
                        {
                            input = input.Remove(0, 8);
                            if (input == "True")
                            {
                                this.checkBoxTrans.Checked = true;
                            }
                            else if (input == "False")
                            {
                                this.checkBoxTrans.Checked = false;
                            }
                        }
                    }
                    catch
                    {
                        //nothing to do
                    }
                }
                re.Close();
            }
            catch
            {
                MessageBox.Show(this.con.getLanguageElement("Configuration.CantLoadConf", "TV-Browser Mini wasn't able to load the configuration"), this.con.getLanguageElement("Configuration.Warning", "Warning!"));
                Application.Exit();
            }
        }

        private void bSave_Click(object sender, EventArgs e)
        {
            Cursor.Current = Cursors.WaitCursor;
            save();
            Cursor.Current = Cursors.Default;
        }

        private void buttonBrowseReminderSound_Click(object sender, EventArgs e)
        {
            this.openFileDialogSound.Filter = "WAV-Files (*.wav)|*.wav";
            this.openFileDialogSound.InitialDirectory = this.con.getSystemPath();
            if (this.openFileDialogSound.ShowDialog() == DialogResult.OK)
            {
                this.textBoxPathReminderSound.Text = this.openFileDialogSound.FileName;
            }
        }

        private void tbPath_TextGotFocus(object sender, EventArgs e)
        {
            this.inputPanel.Enabled = true;
        }


        private void fillComboBoxHours()
        {
            DateTime dt = new DateTime(1982, 5, 24, 0, 0, 0);
            for (int i = 0; i < 24; i++)
            {
                this.comboBoxDayStarts.Items.Add(dt.ToString(this.con.getLanguageElement("Configuration.HourDesign", "hh:mm tt")));
                this.comboBoxDayEnds.Items.Add(dt.ToString(this.con.getLanguageElement("Configuration.HourDesign", "hh:mm tt")));
                dt = dt.AddHours(1);
            }
            this.comboBoxDayStarts.SelectedIndex = 0;
            this.comboBoxDayEnds.SelectedIndex = 2;
        }

        private void fillComboLanguages()
        {
            this.languages = new Hashtable();
            String currentLanguage = "";
            try
            {
                currentLanguage = System.Globalization.CultureInfo.CurrentCulture.EnglishName.Split('(')[0];
            }
            catch
            {
                currentLanguage = "English";
                this.loadedLanguage = "English";
            }
            
            DirectoryInfo info = new DirectoryInfo(this.con.getSystemPath() + "\\languages");
            for (int i = 0; i < info.GetFiles().Length; i++)
            {
                FileInfo file = info.GetFiles()[i];
                String language = file.Name.ToString().Split('.')[0];
                this.comboBoxLanguages.Items.Add(this.con.getLanguageElement("Filesystem.Language."+language, language));
                this.languages.Add(this.con.getLanguageElement("Filesystem.Language."+language,language),language);
                if (currentLanguage.StartsWith(language))
                    this.comboBoxLanguages.SelectedIndex = i;
            }
        }

        void textBoxMinutes_TextChanged(object sender, System.EventArgs e)
        {
            try
            {
                int test = Int32.Parse(this.textBoxMinutes.Text);
            }
            catch
            {
                this.textBoxMinutes.Text = "5";
                MessageBox.Show(this.con.getLanguageElement("Configuration.ParseError","Please insert a possible value for reminder minutes"));
            }
        }

        void textBoxPort_TextChanged(object sender, System.EventArgs e)
        {
            try
            {
                int test = Int32.Parse(this.textBoxPort.Text);
            }
            catch
            {
                this.textBoxPort.Text = "13267";
                MessageBox.Show(this.con.getLanguageElement("Configuration.ParseError", "Please insert a possible value"));
            }
        }


        private void refreshLanguage()
        {
            this.labelPathDB.Text = this.con.getLanguageElement("Configuration.PathToTVData","Path to your TV-Data File (*.tvd)");
            this.tabGeneral.Text = this.con.getLanguageElement("Configuration.TabGeneral", "general");
            this.lLanguage.Text = this.con.getLanguageElement("Configuration.Language", "language");
            this.labelReload.Text = this.con.getLanguageElement("Configuration.RefreshEvery", "refresh every");
            this.bBrowseTVData.Text = this.con.getLanguageElement("Configuration.Browse", "browse");
            this.buttonBrowseReminderSound.Text = this.con.getLanguageElement("Configuration.Browse", "browse");
            this.checkBoxPopupReminders.Text = this.con.getLanguageElement("Configuration.ShowPopups", "Show Popups");
            this.lRemindersMinutes.Text = this.con.getLanguageElement("Configuration.Minutes", "minutes");
            this.labelRemindersMinutes.Text = this.con.getLanguageElement("Configuration.RemindMinutes", "Remind me X minutes before broadcasts beginning");
            this.labelReminderWav.Text = this.con.getLanguageElement("Configuration.PlaySound", "Play this Sound (*.wav)");
            this.Text = this.con.getLanguageElement("Configuration.Text", "Configuration");
            this.comboBoxReload.Items[0] = this.con.getLanguageElement("Configuration.Desginer.Disabled", "-disabled-");
            this.tabReminders.Text = this.con.getLanguageElement("Configuration.ReminderTab","reminders");
            this.lSeconds.Text = this.con.getLanguageElement("Configuration.Seconds", "seconds");
            this.checkBoxSoundReminder.Text = this.con.getLanguageElement("Configuration.PlaySoundReminder", "Play Sound");
            this.labelDayBegin.Text = this.con.getLanguageElement("Configuration.DayBegin", "Day starts at");
            this.labelDayEnds.Text = this.con.getLanguageElement("Configuration.DayEnd", "Day ends at");
            this.labelToday.Text = this.con.getLanguageElement("Configuration.Today", "(today)");
            this.labelTomorrow.Text = this.con.getLanguageElement("Configuration.Tomorrow", "(tomorrow)");
            this.tabSync.Text = this.con.getLanguageElement("Configuration.TabSync","synchronization");
            this.labelSyncTutorial.Text = this.con.getLanguageElement("Configuration.SyncTutorial","To be able to sync TV - data between PDA and PC you have to write your the DNS name or  IP of your device into TV-Browser Mini export plugin configuration. DNS name is preferred when your divce uses multible IPs.Both export plugin and PDA program must use the same port!");
            this.labelDNS.Text = this.con.getLanguageElement("Configuration.DNSName","DNS name");
            this.labelIP.Text = this.con.getLanguageElement("Configuration.IP","IP address");
            this.labelPort.Text = this.con.getLanguageElement("Configuration.Port", "Port");
            this.checkBoxTrans.Text = this.con.getLanguageElement("Configuration.ActivateSync", "activate synchronisation");
        }


        private void tbPath_TextLostFocus(object sender, EventArgs e)
        {
            if (textBoxPathDB.Text.Length > 4)
            {
                this.checkBoxTrans.Enabled = true;
            }
            else
            {
                this.checkBoxTrans.Enabled = false;
                MessageBox.Show(this.con.getLanguageElement("Configuration.ParseError", "Please insert a possible value"));
            }
        }
    }
}

       