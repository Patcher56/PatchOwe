/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.averi.patchowe;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Patrick
 */
public class MainMenu extends javax.swing.JFrame {
	protected static SplashScreen mySplash;
	private static Rectangle2D.Double splashTextArea;
	private static Rectangle2D.Double splashProgressArea;
	private static Graphics2D splashGraphics;
	private static Font fonti;
	private static Color farbe;

	private static void splashinit() {
        mySplash = SplashScreen.getSplashScreen();
        if (mySplash != null)
        {   // if there are any problems displaying the splash this will be null
            Dimension ssDim = mySplash.getSize();
            int height = ssDim.height;
            int width = ssDim.width;
            // stake out some area for our status information
            splashTextArea = new Rectangle2D.Double(0, height*0.88, width * .8, 24.);
            splashProgressArea = new Rectangle2D.Double(width * .82, height*.90, width*.15, 12 );

            // create the Graphics environment for drawing status info
            splashGraphics = mySplash.createGraphics();
			farbe = new Color(255, 255, 255, 255);
            fonti = new Font("Dialog", Font.PLAIN, 14);
            splashGraphics.setFont(fonti);
            
            // initialize the status info
            splashText("Starte...");
            splashProgress(0);
        }
	}

	private static void splashText(String str) {
		if (mySplash != null && mySplash.isVisible())
        {   // important to check here so no other methods need to know if there
            // really is a Splash being displayed

            // erase the last status text
			
            splashGraphics.setPaint(farbe);
            splashGraphics.fill(splashTextArea);

            // draw the text
            splashGraphics.setPaint(Color.BLACK);
            splashGraphics.drawString(str, (int)(splashTextArea.getX() + 10),(int)(splashTextArea.getY() + 15));

            // make sure it's displayed
            mySplash.update();
        }
	}

	private static void splashProgress(int pct) {
		if (mySplash != null && mySplash.isVisible())
        {

            // Note: 3 colors are used here to demonstrate steps
            // erase the old one
            splashGraphics.setPaint(farbe);
            splashGraphics.fill(splashProgressArea);

            // draw an outline
            splashGraphics.setPaint(Color.BLUE);
            splashGraphics.draw(splashProgressArea);

            // Calculate the width corresponding to the correct percentage
            int x = (int) splashProgressArea.getMinX();
            int y = (int) splashProgressArea.getMinY();
            int wid = (int) splashProgressArea.getWidth();
            int hgt = (int) splashProgressArea.getHeight();

            int doneWidth = Math.round(pct*wid/100.f);
            doneWidth = Math.max(0, Math.min(doneWidth, wid-1));  // limit 0-width

            // fill the done part one pixel smaller than the outline
            splashGraphics.setPaint(Color.GREEN);
            splashGraphics.fillRect(x, y+1, doneWidth, hgt-1);

            // make sure it's displayed
            mySplash.update();
        }
	}

	private static void appinit() {
		for(int i=1;i<=2;i++)
        {
            int pctDone = i*50;
            
            splashProgress(pctDone);
            try
            {
                if (i==1) {
					try {
						splashText("Verbindung zur Datenbank wird hergestellt!");
						createConfigFile();
						if (getvariable("FirstStart") != null) {
							MainMenu.FirstStart = Boolean.parseBoolean(p.getProperty("FirstStart"));
						}
						if (MainMenu.FirstStart == true) {
							dlg_firststart.pack();
							dlg_firststart.setLocationRelativeTo(null);
							dlg_firststart.setVisible(true);
						}
						createcon();
						connected=true;
					} catch (Exception ex) {
						MainMenu.errorBox(null, "Verbindung mit der Datenbank konnte nicht hergestellt werden.\n"
								+ "Möglicherweise besteht keine Internetverbindung.\n"
								+ "Das Programm wird jetzt beendet.\n" + ex, "Verbindung konnte nicht hergestellt werden");
						Runtime.getRuntime().exit(1);
					}
					splashText("Verbindung zur Datenbank OK!");
					Thread.sleep(500);
				}
				if (i==2 && connected == true) {
					splashText("Tabellen werden geprüft!");
					MainMenu.tblcheck();
					splashText("Tabellen OK!");
					Thread.sleep(500);
				}
				
				
            }
            catch (InterruptedException ex)
            {
                // ignore it
            }
        }
	}

	//Datenbank Allgemein
	private static String Host;
	private static String DB_Name;
	private static String UserName;
	private static String Password;
	private static String HostURL;
	
	protected static Connection con;
	protected static PreparedStatement stmt;
	protected static String SQL;
	
	//ResultSets
	protected static ResultSet rs;
	private static ResultSet rs_trans_names;
    private ResultSet rs_tabelle_fill;
    private ResultSet rs_tabelle_names;
	private ResultSet rs_info;
	private ResultSet rs_trans_trans;
	private ResultSet rs_trans_ändern;
    
	//Config-Datei
	protected static Properties p;
	protected static File ConfigFile;
	
    //Frames
    NewEntry newentry;
	NewRepayment newpay;
	MyMoney mymoney;
	VirtualPay vpay;
    
	//Integer
	private int anzahlgesamt;
	private int SelectedID;
	private int index_trans_names;
	
    //Doubles
    double Summe;
    double summeschulden = 0;
    double summerückzahlungen = 0;
    protected static double summegesamt;
    
    //Booleans
	protected static boolean connected = false;
	private boolean alletrans;
	private static Boolean FirstStart;
	
    
    //Strings
    String[] splitNames;
	
    
    //Sonstiges
    DecimalFormat f = new DecimalFormat("#0.00 €");
	private SimpleDateFormat d = new SimpleDateFormat("dd.MM.yyyy");
	int si_tabelle_cbox_namen;
	private int MenuStyle;
	private String NewestVersion;
	protected String PatchOweVersion;
	
	
	
	
	
	

	
	/**
	 * Creates new form MainMenu
	 */
	public MainMenu() {
		this.FirstStart = true;
		//Frames
		newentry = new NewEntry();
		newpay = new NewRepayment();
		mymoney = new MyMoney();
		vpay = new VirtualPay();
		
		UIManager.put("control", new Color(240,240,240));
		
		newentry.createcon();
		
		initComponents();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        frm_tabelle = new javax.swing.JFrame();
        scrl_datenscrollbar = new javax.swing.JScrollPane();
        tbl_tabelle_daten = new javax.swing.JTable(){
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false; //Disallow the editing of any cell
            }
        };
        lbl_tabelle_schulden = new javax.swing.JLabel();
        lbl_tabelle_verliehen = new javax.swing.JLabel();
        lbl_tabelle_gesamt = new javax.swing.JLabel();
        txt_tabelle_schulden = new javax.swing.JTextField();
        txt_tabelle_verliehen = new javax.swing.JTextField();
        txt_tabelle_gesamt = new javax.swing.JTextField();
        cbox_tabelle_namen = new javax.swing.JComboBox<String>();
        btn_tabelle_anzeigen = new javax.swing.JButton();
        pup_daten = new javax.swing.JPopupMenu();
        dlg_personenlöschen = new javax.swing.JDialog();
        cbox_deleteperson_friends = new javax.swing.JComboBox<String>();
        btn_deleteperson_löschen = new javax.swing.JButton();
        btn_deleteperson_abbrechen = new javax.swing.JButton();
        dlg_transaktionen = new javax.swing.JDialog();
        cbox_trans_freund = new javax.swing.JComboBox<String>();
        lbl_freund = new javax.swing.JLabel();
        cbox_trans_trans = new javax.swing.JComboBox<String>();
        jLabel1 = new javax.swing.JLabel();
        btn_trans_löschen = new javax.swing.JButton();
        pnl_trans = new javax.swing.JPanel();
        lbl_trans_info_betrag = new javax.swing.JLabel();
        lbl_trans_info_desc = new javax.swing.JLabel();
        lbl_trans_info_datum = new javax.swing.JLabel();
        txt_trans_info_desc = new javax.swing.JTextField();
        txt_trans_info_datum = new javax.swing.JTextField();
        lbl_trans_info_name = new javax.swing.JLabel();
        rdio_trans_schuld = new javax.swing.JRadioButton();
        rdio_trans_verliehen = new javax.swing.JRadioButton();
        cbox_trans_info_names = new javax.swing.JComboBox<String>();
        sp_info_betrag = new javax.swing.JSpinner();
        btn_trans_ändern = new javax.swing.JButton();
        btn_trans_abbrechen = new javax.swing.JButton();
        dlg_firststart = new javax.swing.JDialog();
        btn_firststart_beenden = new javax.swing.JButton();
        btn_firststart_speichern = new javax.swing.JButton();
        pnl_firststart_verbindung = new javax.swing.JPanel();
        txt_firststart_host = new javax.swing.JTextField();
        txt_firststart_dbname = new javax.swing.JTextField();
        lbl_firststart_dbname = new javax.swing.JLabel();
        lbl_firststart_uname = new javax.swing.JLabel();
        lbl_firststart_pass = new javax.swing.JLabel();
        txt_firststart_uname = new javax.swing.JTextField();
        lbl_firststart_host = new javax.swing.JLabel();
        pswd_firststart_passwort = new javax.swing.JPasswordField();
        rdiogrp_trans = new javax.swing.ButtonGroup();
        dlg_optionen = new javax.swing.JDialog();
        pnl_optionen_mysql = new javax.swing.JPanel();
        lbl_optionen_mysql_host = new javax.swing.JLabel();
        txt_optionen_mysql_host = new javax.swing.JTextField();
        lbl_optionen_mysql_dbname = new javax.swing.JLabel();
        txt_optionen_mysql_dbname = new javax.swing.JTextField();
        lbl_optionen_mysql_uname = new javax.swing.JLabel();
        txt_optionen_mysql_uname = new javax.swing.JTextField();
        lbl_optionen_mysql_pass = new javax.swing.JLabel();
        pswd_optionen_mysql_passwort = new javax.swing.JPasswordField();
        btn_optionen_speichern1 = new javax.swing.JButton();
        btn_optionen_abbrechen1 = new javax.swing.JButton();
        pnl_optionen_menü = new javax.swing.JPanel();
        rdio_optionen_menü_showtoolbox = new javax.swing.JRadioButton();
        rdio_optionen_menü_showmenü = new javax.swing.JRadioButton();
        rdio_optionen_menü_showboth = new javax.swing.JRadioButton();
        rdiogrp_optionen_menü = new javax.swing.ButtonGroup();
        dlg_about = new javax.swing.JDialog();
        jLabel2 = new javax.swing.JLabel();
        lbl_about_version = new javax.swing.JLabel();
        lbl_about_newestversion = new javax.swing.JLabel();
        btn_about_schliessen = new javax.swing.JButton();
        btn_about_downloadnewestversion = new javax.swing.JButton();
        lbl_about_info = new javax.swing.JLabel();
        pnl_aktionen = new javax.swing.JPanel();
        btn_neuereintrag = new javax.swing.JButton();
        btn_looktransactions = new javax.swing.JButton();
        btn_neuerückzahlung = new javax.swing.JButton();
        pnl_information = new javax.swing.JPanel();
        lbl_info_balance = new javax.swing.JLabel();
        lbl_database = new javax.swing.JLabel();
        lbl_info_anzahl = new javax.swing.JLabel();
        tbar_home = new javax.swing.JToolBar();
        tbar_btn_addfriend = new javax.swing.JButton();
        tbar_btn_removefriend = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        tbar_btn_mymoney = new javax.swing.JButton();
        tbar_btn_virtualpay = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        tbar_btn_options = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        tbar_btn_about = new javax.swing.JButton();
        tbar_btn_help = new javax.swing.JButton();
        mbar_home = new javax.swing.JMenuBar();
        m_datei = new javax.swing.JMenu();
        m_neu = new javax.swing.JMenu();
        mitem_neueperson = new javax.swing.JMenuItem();
        mitem_allesanzeigen = new javax.swing.JMenuItem();
        m_ändern = new javax.swing.JMenu();
        mitem_transaktionenändernlöschen = new javax.swing.JMenuItem();
        m_löschen = new javax.swing.JMenu();
        mitem_alleslöschen = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mitem_atransaktionenlöschen = new javax.swing.JMenuItem();
        mitem_btransaktionenlöschen = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        mitem_personenlöschen = new javax.swing.JMenuItem();
        mitem_beenden = new javax.swing.JMenuItem();
        m_tools = new javax.swing.JMenu();
        mitem_geldbestand = new javax.swing.JMenuItem();
        mitem_virtuellezahlung = new javax.swing.JMenuItem();
        mitem_optionen = new javax.swing.JMenuItem();
        m_hilfe = new javax.swing.JMenu();
        mitem_hilfe = new javax.swing.JMenuItem();
        mitem_über = new javax.swing.JMenuItem();

        frm_tabelle.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        frm_tabelle.setResizable(false);
        frm_tabelle.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                frm_tabelleWindowLostFocus(evt);
            }
        });
        frm_tabelle.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                frm_tabelleWindowOpened(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                frm_tabelletbl_daten_füllen(evt);
            }
        });

        tbl_tabelle_daten.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null}
            },
            new String [] {
                "Name", "Datum", "Beschreibung", "Transaktion"
            }
        ));
        tbl_tabelle_daten.setRowHeight(30);
        tbl_tabelle_daten.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tbl_tabelle_daten.getTableHeader().setReorderingAllowed(false);
        scrl_datenscrollbar.setViewportView(tbl_tabelle_daten);

        lbl_tabelle_schulden.setText("Schulden:");

        lbl_tabelle_verliehen.setText("Verliehen:");

        lbl_tabelle_gesamt.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        lbl_tabelle_gesamt.setText("Gesamt:");

        txt_tabelle_schulden.setEditable(false);
        txt_tabelle_schulden.setForeground(new java.awt.Color(255, 0, 0));
        txt_tabelle_schulden.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txt_tabelle_schulden.setText("Schulden");
        txt_tabelle_schulden.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

        txt_tabelle_verliehen.setEditable(false);
        txt_tabelle_verliehen.setForeground(new java.awt.Color(51, 153, 0));
        txt_tabelle_verliehen.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txt_tabelle_verliehen.setText("Verliehen");
        txt_tabelle_verliehen.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

        txt_tabelle_gesamt.setEditable(false);
        txt_tabelle_gesamt.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txt_tabelle_gesamt.setText("Gesamt");
        txt_tabelle_gesamt.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));

        btn_tabelle_anzeigen.setText("Anzeigen");
        btn_tabelle_anzeigen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_tabelle_anzeigenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout frm_tabelleLayout = new javax.swing.GroupLayout(frm_tabelle.getContentPane());
        frm_tabelle.getContentPane().setLayout(frm_tabelleLayout);
        frm_tabelleLayout.setHorizontalGroup(
            frm_tabelleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(scrl_datenscrollbar, javax.swing.GroupLayout.DEFAULT_SIZE, 539, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, frm_tabelleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbox_tabelle_namen, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_tabelle_anzeigen)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(frm_tabelleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbl_tabelle_verliehen)
                    .addComponent(lbl_tabelle_schulden)
                    .addComponent(lbl_tabelle_gesamt))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(frm_tabelleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txt_tabelle_verliehen)
                    .addComponent(txt_tabelle_gesamt, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txt_tabelle_schulden, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        frm_tabelleLayout.setVerticalGroup(
            frm_tabelleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(frm_tabelleLayout.createSequentialGroup()
                .addComponent(scrl_datenscrollbar, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(frm_tabelleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_tabelle_schulden)
                    .addComponent(txt_tabelle_schulden, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbox_tabelle_namen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btn_tabelle_anzeigen))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(frm_tabelleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_tabelle_verliehen)
                    .addComponent(txt_tabelle_verliehen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(frm_tabelleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_tabelle_gesamt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_tabelle_gesamt))
                .addContainerGap())
        );

        pup_daten.setLabel("TEST");

        dlg_personenlöschen.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        dlg_personenlöschen.setTitle("Personen löschen");
        dlg_personenlöschen.setModal(true);
        dlg_personenlöschen.setResizable(false);
        dlg_personenlöschen.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                dlg_personenlöschenWindowActivated(evt);
            }
        });

        cbox_deleteperson_friends.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        btn_deleteperson_löschen.setText("Löschen");
        btn_deleteperson_löschen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_deleteperson_löschenActionPerformed(evt);
            }
        });

        btn_deleteperson_abbrechen.setText("Abbrechen");
        btn_deleteperson_abbrechen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_deleteperson_abbrechenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dlg_personenlöschenLayout = new javax.swing.GroupLayout(dlg_personenlöschen.getContentPane());
        dlg_personenlöschen.getContentPane().setLayout(dlg_personenlöschenLayout);
        dlg_personenlöschenLayout.setHorizontalGroup(
            dlg_personenlöschenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dlg_personenlöschenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dlg_personenlöschenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(cbox_deleteperson_friends, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(dlg_personenlöschenLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btn_deleteperson_abbrechen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_deleteperson_löschen)))
                .addContainerGap())
        );
        dlg_personenlöschenLayout.setVerticalGroup(
            dlg_personenlöschenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlg_personenlöschenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbox_deleteperson_friends, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(dlg_personenlöschenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_deleteperson_löschen)
                    .addComponent(btn_deleteperson_abbrechen))
                .addContainerGap())
        );

        dlg_personenlöschen.getAccessibleContext().setAccessibleParent(this);

        dlg_transaktionen.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        dlg_transaktionen.setTitle("Transaktionen ändern/löschen");
        dlg_transaktionen.setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg_transaktionen.setResizable(false);
        dlg_transaktionen.addWindowFocusListener(new java.awt.event.WindowFocusListener() {
            public void windowGainedFocus(java.awt.event.WindowEvent evt) {
            }
            public void windowLostFocus(java.awt.event.WindowEvent evt) {
                dlg_transaktionenWindowLostFocus(evt);
            }
        });
        dlg_transaktionen.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                dlg_transaktionenWindowActivated(evt);
            }
        });

        cbox_trans_freund.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbox_trans_freund.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbox_trans_freundActionPerformed(evt);
            }
        });

        lbl_freund.setText("Name auswählen:");

        cbox_trans_trans.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbox_trans_trans.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbox_trans_transActionPerformed(evt);
            }
        });

        jLabel1.setText("Transaktion auswählen:");

        btn_trans_löschen.setText("Löschen");
        btn_trans_löschen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_trans_löschenActionPerformed(evt);
            }
        });

        pnl_trans.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));

        lbl_trans_info_betrag.setText("Betrag:");

        lbl_trans_info_desc.setText("Beschreibung:");

        lbl_trans_info_datum.setText("Datum:");

        txt_trans_info_desc.setText("beschreibung");

        txt_trans_info_datum.setText("datum");

        lbl_trans_info_name.setText("Name:");

        rdiogrp_trans.add(rdio_trans_schuld);
        rdio_trans_schuld.setText("Schuld");
        rdio_trans_schuld.setFocusable(false);

        rdiogrp_trans.add(rdio_trans_verliehen);
        rdio_trans_verliehen.setText("Verliehen");
        rdio_trans_verliehen.setFocusable(false);

        cbox_trans_info_names.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        sp_info_betrag.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), null, Double.valueOf(0.5d)));
        sp_info_betrag.setEditor(new javax.swing.JSpinner.NumberEditor(sp_info_betrag, "0.00"));

        javax.swing.GroupLayout pnl_transLayout = new javax.swing.GroupLayout(pnl_trans);
        pnl_trans.setLayout(pnl_transLayout);
        pnl_transLayout.setHorizontalGroup(
            pnl_transLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_transLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_transLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lbl_trans_info_name)
                    .addComponent(lbl_trans_info_datum)
                    .addComponent(lbl_trans_info_desc)
                    .addComponent(lbl_trans_info_betrag))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_transLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txt_trans_info_datum, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txt_trans_info_desc)
                    .addComponent(cbox_trans_info_names, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnl_transLayout.createSequentialGroup()
                        .addComponent(rdio_trans_schuld)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rdio_trans_verliehen)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(sp_info_betrag))
                .addContainerGap())
        );
        pnl_transLayout.setVerticalGroup(
            pnl_transLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_transLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnl_transLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdio_trans_verliehen)
                    .addComponent(rdio_trans_schuld))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_transLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_trans_info_name)
                    .addComponent(cbox_trans_info_names, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_transLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_trans_info_betrag)
                    .addComponent(sp_info_betrag, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_transLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_trans_info_desc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_trans_info_desc))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_transLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_trans_info_datum, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_trans_info_datum))
                .addContainerGap())
        );

        btn_trans_ändern.setText("Ändern");
        btn_trans_ändern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_trans_ändernActionPerformed(evt);
            }
        });

        btn_trans_abbrechen.setText("Abbrechen");
        btn_trans_abbrechen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_trans_abbrechenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dlg_transaktionenLayout = new javax.swing.GroupLayout(dlg_transaktionen.getContentPane());
        dlg_transaktionen.getContentPane().setLayout(dlg_transaktionenLayout);
        dlg_transaktionenLayout.setHorizontalGroup(
            dlg_transaktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlg_transaktionenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dlg_transaktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dlg_transaktionenLayout.createSequentialGroup()
                        .addGroup(dlg_transaktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(lbl_freund))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(dlg_transaktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbox_trans_freund, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(cbox_trans_trans, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(pnl_trans, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dlg_transaktionenLayout.createSequentialGroup()
                        .addGap(0, 55, Short.MAX_VALUE)
                        .addComponent(btn_trans_abbrechen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_trans_löschen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_trans_ändern)))
                .addContainerGap())
        );
        dlg_transaktionenLayout.setVerticalGroup(
            dlg_transaktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlg_transaktionenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dlg_transaktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_freund)
                    .addComponent(cbox_trans_freund, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dlg_transaktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(cbox_trans_trans, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnl_trans, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(dlg_transaktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_trans_ändern)
                    .addComponent(btn_trans_löschen)
                    .addComponent(btn_trans_abbrechen))
                .addContainerGap())
        );

        dlg_firststart.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        dlg_firststart.setTitle("Erster Start - Konfiguration");
        dlg_firststart.setModalityType(java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg_firststart.setResizable(false);
        dlg_firststart.setType(java.awt.Window.Type.UTILITY);

        btn_firststart_beenden.setText("Beenden");
        btn_firststart_beenden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_firststart_beendenActionPerformed(evt);
            }
        });

        btn_firststart_speichern.setText("Speichern");
        btn_firststart_speichern.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_firststart_speichernActionPerformed(evt);
            }
        });

        pnl_firststart_verbindung.setBorder(javax.swing.BorderFactory.createTitledBorder("MySQL-Verbindung"));

        lbl_firststart_dbname.setText("DB-Name:");

        lbl_firststart_uname.setText("Benutzername:");

        lbl_firststart_pass.setText("Passwort:");

        lbl_firststart_host.setText("Host:");

        javax.swing.GroupLayout pnl_firststart_verbindungLayout = new javax.swing.GroupLayout(pnl_firststart_verbindung);
        pnl_firststart_verbindung.setLayout(pnl_firststart_verbindungLayout);
        pnl_firststart_verbindungLayout.setHorizontalGroup(
            pnl_firststart_verbindungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_firststart_verbindungLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_firststart_verbindungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnl_firststart_verbindungLayout.createSequentialGroup()
                        .addGroup(pnl_firststart_verbindungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbl_firststart_host)
                            .addComponent(lbl_firststart_dbname))
                        .addGap(29, 29, 29)
                        .addGroup(pnl_firststart_verbindungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_firststart_dbname)
                            .addComponent(txt_firststart_host)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnl_firststart_verbindungLayout.createSequentialGroup()
                        .addGroup(pnl_firststart_verbindungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbl_firststart_uname)
                            .addComponent(lbl_firststart_pass))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnl_firststart_verbindungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_firststart_uname)
                            .addComponent(pswd_firststart_passwort))))
                .addContainerGap())
        );
        pnl_firststart_verbindungLayout.setVerticalGroup(
            pnl_firststart_verbindungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_firststart_verbindungLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_firststart_verbindungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_firststart_host, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_firststart_host))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_firststart_verbindungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_firststart_dbname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_firststart_dbname))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_firststart_verbindungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_firststart_uname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_firststart_uname))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_firststart_verbindungLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_firststart_pass)
                    .addComponent(pswd_firststart_passwort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout dlg_firststartLayout = new javax.swing.GroupLayout(dlg_firststart.getContentPane());
        dlg_firststart.getContentPane().setLayout(dlg_firststartLayout);
        dlg_firststartLayout.setHorizontalGroup(
            dlg_firststartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlg_firststartLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dlg_firststartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnl_firststart_verbindung, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(dlg_firststartLayout.createSequentialGroup()
                        .addGap(0, 157, Short.MAX_VALUE)
                        .addComponent(btn_firststart_beenden)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_firststart_speichern)))
                .addContainerGap())
        );
        dlg_firststartLayout.setVerticalGroup(
            dlg_firststartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlg_firststartLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_firststart_verbindung, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(dlg_firststartLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_firststart_speichern)
                    .addComponent(btn_firststart_beenden))
                .addContainerGap())
        );

        dlg_optionen.setTitle("Optionen");
        dlg_optionen.setResizable(false);
        dlg_optionen.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                dlg_optionenWindowActivated(evt);
            }
        });

        pnl_optionen_mysql.setBorder(javax.swing.BorderFactory.createTitledBorder("MySQL-Verbindung"));

        lbl_optionen_mysql_host.setText("Host:");

        lbl_optionen_mysql_dbname.setText("DB-Name:");

        lbl_optionen_mysql_uname.setText("Benutzername:");

        lbl_optionen_mysql_pass.setText("Passwort:");

        javax.swing.GroupLayout pnl_optionen_mysqlLayout = new javax.swing.GroupLayout(pnl_optionen_mysql);
        pnl_optionen_mysql.setLayout(pnl_optionen_mysqlLayout);
        pnl_optionen_mysqlLayout.setHorizontalGroup(
            pnl_optionen_mysqlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnl_optionen_mysqlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_optionen_mysqlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnl_optionen_mysqlLayout.createSequentialGroup()
                        .addGroup(pnl_optionen_mysqlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbl_optionen_mysql_host)
                            .addComponent(lbl_optionen_mysql_dbname))
                        .addGap(29, 29, 29)
                        .addGroup(pnl_optionen_mysqlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_optionen_mysql_dbname)
                            .addComponent(txt_optionen_mysql_host)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnl_optionen_mysqlLayout.createSequentialGroup()
                        .addGroup(pnl_optionen_mysqlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbl_optionen_mysql_uname)
                            .addComponent(lbl_optionen_mysql_pass))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnl_optionen_mysqlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txt_optionen_mysql_uname)
                            .addComponent(pswd_optionen_mysql_passwort))))
                .addContainerGap())
        );
        pnl_optionen_mysqlLayout.setVerticalGroup(
            pnl_optionen_mysqlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_optionen_mysqlLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_optionen_mysqlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_optionen_mysql_host, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_optionen_mysql_host))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_optionen_mysqlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_optionen_mysql_dbname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_optionen_mysql_dbname))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_optionen_mysqlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txt_optionen_mysql_uname, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_optionen_mysql_uname))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnl_optionen_mysqlLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_optionen_mysql_pass)
                    .addComponent(pswd_optionen_mysql_passwort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btn_optionen_speichern1.setText("Speichern");
        btn_optionen_speichern1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_optionen_speichern1ActionPerformed(evt);
            }
        });

        btn_optionen_abbrechen1.setText("Abbrechen");
        btn_optionen_abbrechen1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_optionen_abbrechen1ActionPerformed(evt);
            }
        });

        pnl_optionen_menü.setBorder(javax.swing.BorderFactory.createTitledBorder("Menü"));

        rdiogrp_optionen_menü.add(rdio_optionen_menü_showtoolbox);
        rdio_optionen_menü_showtoolbox.setText("Toolbox zeigen");

        rdiogrp_optionen_menü.add(rdio_optionen_menü_showmenü);
        rdio_optionen_menü_showmenü.setText("Menü zeigen");

        rdiogrp_optionen_menü.add(rdio_optionen_menü_showboth);
        rdio_optionen_menü_showboth.setText("Beides zeigen");

        javax.swing.GroupLayout pnl_optionen_menüLayout = new javax.swing.GroupLayout(pnl_optionen_menü);
        pnl_optionen_menü.setLayout(pnl_optionen_menüLayout);
        pnl_optionen_menüLayout.setHorizontalGroup(
            pnl_optionen_menüLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_optionen_menüLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdio_optionen_menü_showtoolbox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rdio_optionen_menü_showmenü)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rdio_optionen_menü_showboth)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnl_optionen_menüLayout.setVerticalGroup(
            pnl_optionen_menüLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_optionen_menüLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_optionen_menüLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdio_optionen_menü_showtoolbox)
                    .addComponent(rdio_optionen_menü_showmenü)
                    .addComponent(rdio_optionen_menü_showboth))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout dlg_optionenLayout = new javax.swing.GroupLayout(dlg_optionen.getContentPane());
        dlg_optionen.getContentPane().setLayout(dlg_optionenLayout);
        dlg_optionenLayout.setHorizontalGroup(
            dlg_optionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlg_optionenLayout.createSequentialGroup()
                .addGroup(dlg_optionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(dlg_optionenLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btn_optionen_abbrechen1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_optionen_speichern1))
                    .addGroup(dlg_optionenLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(dlg_optionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnl_optionen_mysql, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnl_optionen_menü, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        dlg_optionenLayout.setVerticalGroup(
            dlg_optionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlg_optionenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnl_optionen_mysql, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_optionen_menü, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(dlg_optionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_optionen_speichern1)
                    .addComponent(btn_optionen_abbrechen1))
                .addContainerGap())
        );

        dlg_about.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        dlg_about.setTitle("Über PatchOwe");
        dlg_about.setResizable(false);
        dlg_about.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                dlg_aboutWindowActivated(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                dlg_aboutWindowClosed(evt);
            }
        });

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/averi/patchowe/pics/PatchOwe-Small.png"))); // NOI18N

        lbl_about_version.setText("Du verwendest PatchOwe Version");

        lbl_about_newestversion.setText("Neueste Version");

        btn_about_schliessen.setText("Schließen");
        btn_about_schliessen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_about_schliessenActionPerformed(evt);
            }
        });

        btn_about_downloadnewestversion.setText("Neueste Version herunterladen");
        btn_about_downloadnewestversion.setEnabled(false);

        lbl_about_info.setText("Information.......");
        lbl_about_info.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout dlg_aboutLayout = new javax.swing.GroupLayout(dlg_about.getContentPane());
        dlg_about.getContentPane().setLayout(dlg_aboutLayout);
        dlg_aboutLayout.setHorizontalGroup(
            dlg_aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlg_aboutLayout.createSequentialGroup()
                .addGap(56, 56, 56)
                .addComponent(jLabel2)
                .addContainerGap(60, Short.MAX_VALUE))
            .addGroup(dlg_aboutLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dlg_aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dlg_aboutLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btn_about_downloadnewestversion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btn_about_schliessen))
                    .addGroup(dlg_aboutLayout.createSequentialGroup()
                        .addGroup(dlg_aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lbl_about_info)
                            .addComponent(lbl_about_newestversion)
                            .addComponent(lbl_about_version))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        dlg_aboutLayout.setVerticalGroup(
            dlg_aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dlg_aboutLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lbl_about_version)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_about_newestversion)
                .addGap(18, 18, 18)
                .addComponent(lbl_about_info)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 153, Short.MAX_VALUE)
                .addGroup(dlg_aboutLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btn_about_schliessen)
                    .addComponent(btn_about_downloadnewestversion))
                .addContainerGap())
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("PatchOwe - Schulden");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
        });

        pnl_aktionen.setBorder(javax.swing.BorderFactory.createTitledBorder("Aktionen"));

        btn_neuereintrag.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/averi/patchowe/pics/badge-square-plus-24-ns.png"))); // NOI18N
        btn_neuereintrag.setText("Neuer Schuldeneintrag");
        btn_neuereintrag.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btn_neuereintrag.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btn_neuereintrag.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_neuereintragActionPerformed(evt);
            }
        });

        btn_looktransactions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/averi/patchowe/pics/badge-square-check-24-ns.png"))); // NOI18N
        btn_looktransactions.setText("Transaktionen ansehen");
        btn_looktransactions.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btn_looktransactions.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btn_looktransactions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_looktransactionsActionPerformed(evt);
            }
        });

        btn_neuerückzahlung.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/averi/patchowe/pics/badge-square-direction-left-24-ns.png"))); // NOI18N
        btn_neuerückzahlung.setText("Neue Rückzahlung");
        btn_neuerückzahlung.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btn_neuerückzahlung.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btn_neuerückzahlung.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_neuerückzahlungActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnl_aktionenLayout = new javax.swing.GroupLayout(pnl_aktionen);
        pnl_aktionen.setLayout(pnl_aktionenLayout);
        pnl_aktionenLayout.setHorizontalGroup(
            pnl_aktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_aktionenLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_aktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btn_neuereintrag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btn_looktransactions, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btn_neuerückzahlung, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnl_aktionenLayout.setVerticalGroup(
            pnl_aktionenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_aktionenLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btn_neuereintrag)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_looktransactions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btn_neuerückzahlung)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnl_information.setBorder(javax.swing.BorderFactory.createTitledBorder("Information"));

        lbl_info_balance.setText("Balance wird geladen...");

        lbl_database.setText("Datenbank-Verbindung wird aufgebaut...");

        lbl_info_anzahl.setText("Anzahl wird geladen...");

        javax.swing.GroupLayout pnl_informationLayout = new javax.swing.GroupLayout(pnl_information);
        pnl_information.setLayout(pnl_informationLayout);
        pnl_informationLayout.setHorizontalGroup(
            pnl_informationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_informationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnl_informationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lbl_database)
                    .addComponent(lbl_info_balance)
                    .addComponent(lbl_info_anzahl))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnl_informationLayout.setVerticalGroup(
            pnl_informationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnl_informationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lbl_info_anzahl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_info_balance)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lbl_database)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tbar_home.setFloatable(false);
        tbar_home.setRollover(true);
        tbar_home.setMinimumSize(new java.awt.Dimension(235, 0));
        tbar_home.setPreferredSize(new java.awt.Dimension(100, 35));

        tbar_btn_addfriend.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/averi/patchowe/pics/person-plus-24-ns.png"))); // NOI18N
        tbar_btn_addfriend.setToolTipText("Person hinzufügen");
        tbar_btn_addfriend.setFocusable(false);
        tbar_btn_addfriend.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbar_btn_addfriend.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbar_btn_addfriend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbar_btn_addfriendActionPerformed(evt);
            }
        });
        tbar_home.add(tbar_btn_addfriend);

        tbar_btn_removefriend.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/averi/patchowe/pics/person-minus-24-ns.png"))); // NOI18N
        tbar_btn_removefriend.setToolTipText("Person löschen");
        tbar_btn_removefriend.setFocusable(false);
        tbar_btn_removefriend.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbar_btn_removefriend.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbar_btn_removefriend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbar_btn_removefriendActionPerformed(evt);
            }
        });
        tbar_home.add(tbar_btn_removefriend);
        tbar_home.add(jSeparator3);

        tbar_btn_mymoney.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/averi/patchowe/pics/money.png"))); // NOI18N
        tbar_btn_mymoney.setToolTipText("Mein Geldbestand");
        tbar_btn_mymoney.setFocusable(false);
        tbar_btn_mymoney.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbar_btn_mymoney.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbar_btn_mymoneyActionPerformed(evt);
            }
        });
        tbar_home.add(tbar_btn_mymoney);

        tbar_btn_virtualpay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/averi/patchowe/pics/coin_stack_silver_add.png"))); // NOI18N
        tbar_btn_virtualpay.setToolTipText("Virtuelle Zahlung");
        tbar_btn_virtualpay.setFocusable(false);
        tbar_btn_virtualpay.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbar_btn_virtualpay.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbar_btn_virtualpay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbar_btn_virtualpayActionPerformed(evt);
            }
        });
        tbar_home.add(tbar_btn_virtualpay);
        tbar_home.add(jSeparator4);

        tbar_btn_options.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/averi/patchowe/pics/preferences_desktop.png"))); // NOI18N
        tbar_btn_options.setToolTipText("Optionen");
        tbar_btn_options.setFocusable(false);
        tbar_btn_options.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbar_btn_options.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbar_btn_options.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbar_btn_optionsActionPerformed(evt);
            }
        });
        tbar_home.add(tbar_btn_options);
        tbar_home.add(jSeparator5);

        tbar_btn_about.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/averi/patchowe/pics/about.png"))); // NOI18N
        tbar_btn_about.setToolTipText("Über");
        tbar_btn_about.setFocusable(false);
        tbar_btn_about.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbar_btn_about.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbar_btn_about.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbar_btn_aboutActionPerformed(evt);
            }
        });
        tbar_home.add(tbar_btn_about);

        tbar_btn_help.setIcon(new javax.swing.ImageIcon(getClass().getResource("/at/averi/patchowe/pics/help.png"))); // NOI18N
        tbar_btn_help.setToolTipText("Hilfe");
        tbar_btn_help.setEnabled(false);
        tbar_btn_help.setFocusable(false);
        tbar_btn_help.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        tbar_btn_help.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        tbar_home.add(tbar_btn_help);

        m_datei.setText("Datei");

        m_neu.setText("Neu");

        mitem_neueperson.setText("Person hinzufügen...");
        mitem_neueperson.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitem_neuepersonActionPerformed(evt);
            }
        });
        m_neu.add(mitem_neueperson);

        m_datei.add(m_neu);

        mitem_allesanzeigen.setText("Alle Daten anzeigen...");
        mitem_allesanzeigen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitem_allesanzeigenActionPerformed(evt);
            }
        });
        m_datei.add(mitem_allesanzeigen);

        m_ändern.setText("Daten ändern");

        mitem_transaktionenändernlöschen.setText("Transaktionen ändern...");
        mitem_transaktionenändernlöschen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitem_transaktionenändernlöschenActionPerformed(evt);
            }
        });
        m_ändern.add(mitem_transaktionenändernlöschen);

        m_datei.add(m_ändern);

        m_löschen.setText("Daten löschen");

        mitem_alleslöschen.setText("Alle Daten löschen...");
        mitem_alleslöschen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitem_alleslöschenActionPerformed(evt);
            }
        });
        m_löschen.add(mitem_alleslöschen);
        m_löschen.add(jSeparator1);

        mitem_atransaktionenlöschen.setText("Alle Transaktionen löschen...");
        mitem_atransaktionenlöschen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitem_atransaktionenlöschenActionPerformed(evt);
            }
        });
        m_löschen.add(mitem_atransaktionenlöschen);

        mitem_btransaktionenlöschen.setText("Bestimmte Transaktionen löschen...");
        mitem_btransaktionenlöschen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitem_btransaktionenlöschenActionPerformed(evt);
            }
        });
        m_löschen.add(mitem_btransaktionenlöschen);
        m_löschen.add(jSeparator2);

        mitem_personenlöschen.setText("Personen löschen...");
        mitem_personenlöschen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitem_personenlöschenActionPerformed(evt);
            }
        });
        m_löschen.add(mitem_personenlöschen);

        m_datei.add(m_löschen);

        mitem_beenden.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0));
        mitem_beenden.setText("Beenden");
        mitem_beenden.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitem_beendenActionPerformed(evt);
            }
        });
        m_datei.add(mitem_beenden);

        mbar_home.add(m_datei);

        m_tools.setText("Tools");

        mitem_geldbestand.setText("Mein Geldbestand...");
        mitem_geldbestand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitem_geldbestandActionPerformed(evt);
            }
        });
        m_tools.add(mitem_geldbestand);

        mitem_virtuellezahlung.setText("Virtuelle Zahlung...");
        mitem_virtuellezahlung.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitem_virtuellezahlungActionPerformed(evt);
            }
        });
        m_tools.add(mitem_virtuellezahlung);

        mitem_optionen.setText("Optionen...");
        mitem_optionen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitem_optionenActionPerformed(evt);
            }
        });
        m_tools.add(mitem_optionen);

        mbar_home.add(m_tools);

        m_hilfe.setText("Hilfe");

        mitem_hilfe.setText("Online Hilfe");
        mitem_hilfe.setEnabled(false);
        m_hilfe.add(mitem_hilfe);

        mitem_über.setText("Über");
        mitem_über.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mitem_überActionPerformed(evt);
            }
        });
        m_hilfe.add(mitem_über);

        mbar_home.add(m_hilfe);

        setJMenuBar(mbar_home);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnl_information, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnl_aktionen, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
            .addComponent(tbar_home, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(tbar_home, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_aktionen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_information, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void mitem_allesanzeigenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitem_allesanzeigenActionPerformed
        alletrans=true;
		frm_tabelle.pack();
        frm_tabelle.setVisible(true);
        frm_tabelle.setTitle("Alle Transaktionen");
    }//GEN-LAST:event_mitem_allesanzeigenActionPerformed

    private void mitem_alleslöschenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitem_alleslöschenActionPerformed

        Object[] options = {"Ja", "Nein",};
        if (JOptionPane.showOptionDialog(this,"Willst du wirklich alle Daten löschen?",
            "Alle Daten löschen?",	JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1])==0) {
        try {
            SQL = "DELETE FROM patchowe_transaktionen";
            MainMenu.prepareStatement();
            stmt.execute();
            SQL = "DELETE FROM patchowe_personen";
            MainMenu.prepareStatement();
            stmt.execute();
            infoBox(this, "Alle Daten wurden gelöscht!", "Daten gelöscht!");
            newentry.cboxrefresh();
        } catch (Exception ex) {
            MainMenu.errorBox(this, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
        }}
    }//GEN-LAST:event_mitem_alleslöschenActionPerformed

    private void mitem_atransaktionenlöschenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitem_atransaktionenlöschenActionPerformed
        Object[] options = {"Ja", "Nein",};
        if (JOptionPane.showOptionDialog(this,"Willst du wirklich alle Transaktionen löschen?",
            "Alle Transaktionen löschen?",	JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[1])==0) {
        try {
            SQL = "DELETE FROM patchowe_transaktionen";
            MainMenu.prepareStatement();
            stmt.execute();
            infoBox(this, "Alle Transaktionen wurden gelöscht!", "Transaktionen gelöscht!");
            newentry.cboxrefresh();
        } catch (Exception ex) {
            errorBox(this, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
        }
        }
    }//GEN-LAST:event_mitem_atransaktionenlöschenActionPerformed

    private void mitem_beendenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitem_beendenActionPerformed
        Runtime.getRuntime().exit(1);
    }//GEN-LAST:event_mitem_beendenActionPerformed

    private void btn_neuereintragActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_neuereintragActionPerformed
        newentry.pack();
		newentry.setVisible(true);
    }//GEN-LAST:event_btn_neuereintragActionPerformed

    private void btn_looktransactionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_looktransactionsActionPerformed
			frm_tabelle.pack();
			frm_tabelle.setLocationRelativeTo(null);
            frm_tabelle.setVisible(true);
            frm_tabelle.setTitle("Transaktionen");

    }//GEN-LAST:event_btn_looktransactionsActionPerformed

    private void frm_tabelletbl_daten_füllen(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_frm_tabelletbl_daten_füllen
        if (si_tabelle_cbox_namen == 0) {
			tbl_showall();
		}
        tbl_cbox_names_refresh();

    }//GEN-LAST:event_frm_tabelletbl_daten_füllen

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        	
		
		
		updateinfo();
		refreshMenuStyle();
		PatchOweVersion = "0.1.0";
    }//GEN-LAST:event_formWindowOpened

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
		updateinfo();		
		refreshMenuStyle();
		
    }//GEN-LAST:event_formWindowActivated

    private void frm_tabelleWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_frm_tabelleWindowLostFocus
		si_tabelle_cbox_namen = cbox_tabelle_namen.getSelectedIndex();
    }//GEN-LAST:event_frm_tabelleWindowLostFocus

    private void btn_tabelle_anzeigenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_tabelle_anzeigenActionPerformed
        if (cbox_tabelle_namen.getSelectedIndex() == 0) {
			tbl_showall();
		}
		else {
			fillTableWithName();
			tbl_info_forname();
		}
        
    }//GEN-LAST:event_btn_tabelle_anzeigenActionPerformed

    private void frm_tabelleWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_frm_tabelleWindowOpened
		JMenuItem menu1 = new JMenuItem( "Eintrag 1");
		pup_daten.add( menu1 );
		tbl_tabelle_daten.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3){
					System.out.println("RECHTS");
					int r = tbl_tabelle_daten.rowAtPoint(e.getPoint());
					if (r >= 0 && r < tbl_tabelle_daten.getRowCount()) {
						tbl_tabelle_daten.setRowSelectionInterval(r, r);
					} else {
						tbl_tabelle_daten.clearSelection();
					}
					int rowindex = tbl_tabelle_daten.getSelectedRow();
					if (rowindex < 0)
						return;
					//if (e.isPopupTrigger() && e.getComponent() instanceof JTable ) {
						pup_daten.show(e.getComponent(), e.getX(), e.getY());
					//}
				} 
                    
            }
		});
    }//GEN-LAST:event_frm_tabelleWindowOpened

    private void btn_neuerückzahlungActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_neuerückzahlungActionPerformed
		newpay.pack();
		newpay.setVisible(true);
    }//GEN-LAST:event_btn_neuerückzahlungActionPerformed

    private void mitem_personenlöschenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitem_personenlöschenActionPerformed
        dlg_personenlöschen.pack();
		dlg_personenlöschen.setLocationRelativeTo(null);
		dlg_personenlöschen.setVisible(true);
    }//GEN-LAST:event_mitem_personenlöschenActionPerformed

    private void dlg_personenlöschenWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_dlg_personenlöschenWindowActivated
		try {
			MainMenu.SQL="SELECT id, vorname, nachname FROM patchowe_personen";
			MainMenu.prepareStatement();
			MainMenu.rs = MainMenu.stmt.executeQuery();
			cbox_deleteperson_friends.removeAllItems();
			while (MainMenu.rs.next()) {
				cbox_deleteperson_friends.addItem(MainMenu.rs.getInt("id") + " - " + MainMenu.rs.getString("vorname") + " " + MainMenu.rs.getString("nachname"));
			}
		} catch (SQLException ex) {
			errorBox(this, "Verbindung mit der Offline-Datenbank konnte nicht hergestellt werden.\nFehlermeldung:\n" + ex.getMessage(), "Fehler!");
		}
    }//GEN-LAST:event_dlg_personenlöschenWindowActivated

    private void btn_deleteperson_abbrechenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_deleteperson_abbrechenActionPerformed
        dlg_personenlöschen.dispose();
    }//GEN-LAST:event_btn_deleteperson_abbrechenActionPerformed

    private void btn_deleteperson_löschenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_deleteperson_löschenActionPerformed
		try {
			String sel;
			sel = cbox_deleteperson_friends.getSelectedItem().toString();
			String splitResult[] = sel.split(" ");
			
			MainMenu.SQL="DELETE FROM patchowe_transaktionen WHERE pid = ?";
			MainMenu.prepareStatement();
			MainMenu.stmt.setString(1, splitResult[0]);
			MainMenu.stmt.execute();
			
			MainMenu.SQL="DELETE FROM patchowe_personen WHERE id = ?";
			MainMenu.prepareStatement();
			MainMenu.stmt.setString(1, splitResult[0]);
			MainMenu.stmt.execute();
			MainMenu.infoBox(this, "Die Person " + cbox_deleteperson_friends.getSelectedItem().toString() + " wurde erfolgreich aus der Datenbank gelöscht!",
		    "Erfolgreich gelöscht!");
		} catch (SQLException ex) {
			errorBox(this, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
		}
		
    }//GEN-LAST:event_btn_deleteperson_löschenActionPerformed

    private void mitem_neuepersonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitem_neuepersonActionPerformed
        NewFriend newfriend = new NewFriend();
        newfriend.pack();
		newfriend.setVisible(true);
    }//GEN-LAST:event_mitem_neuepersonActionPerformed

    private void mitem_btransaktionenlöschenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitem_btransaktionenlöschenActionPerformed
        dlg_transaktionen.pack();
		dlg_transaktionen.setLocationRelativeTo(null);
		dlg_transaktionen.setVisible(true);
    }//GEN-LAST:event_mitem_btransaktionenlöschenActionPerformed

    private void cbox_trans_freundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbox_trans_freundActionPerformed
        if (cbox_trans_freund.getSelectedItem() != null) {
            transrefresh();
        }

    }//GEN-LAST:event_cbox_trans_freundActionPerformed

    private void cbox_trans_transActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbox_trans_transActionPerformed
        if (cbox_trans_trans.getSelectedItem() != null) {
            trans_inforefresh();
        }
    }//GEN-LAST:event_cbox_trans_transActionPerformed

    private void btn_trans_abbrechenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_trans_abbrechenActionPerformed
        dlg_transaktionen.dispose();
    }//GEN-LAST:event_btn_trans_abbrechenActionPerformed

    private void mitem_transaktionenändernlöschenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitem_transaktionenändernlöschenActionPerformed
        dlg_transaktionen.pack();
		dlg_transaktionen.setLocationRelativeTo(null);
		dlg_transaktionen.setVisible(true);
    }//GEN-LAST:event_mitem_transaktionenändernlöschenActionPerformed

    private void dlg_transaktionenWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_dlg_transaktionenWindowActivated
        if (index_trans_names > 0) {
			cbox_trans_freund.setSelectedIndex(index_trans_names);
		}
		trans_namesrefresh();
    }//GEN-LAST:event_dlg_transaktionenWindowActivated

    private void btn_trans_ändernActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_trans_ändernActionPerformed
		try {
			String sel;
			sel = cbox_trans_info_names.getSelectedItem().toString();
			String[] splitResult = sel.split(" ");
			String trans_idString = null;
			java.util.Date UtilDate = d.parse(txt_trans_info_datum.getText());
			java.sql.Date SQLDate = new java.sql.Date(UtilDate.getTime());
			
			MainMenu.SQL = "SELECT id FROM patchowe_personen WHERE vorname = ? AND nachname = ?";
			MainMenu.prepareStatement();
			MainMenu.stmt.setString(1, splitResult[0]);
			MainMenu.stmt.setString(2, splitResult[1]);
			rs_trans_ändern = MainMenu.stmt.executeQuery();
			
			if (rs_trans_ändern.next()) {
				trans_idString=rs_trans_ändern.getString(1);
			}

			
			MainMenu.SQL = "UPDATE patchowe_transaktionen SET pid = ?, betrag = ?, beschreibung = ?, datum = ? WHERE id = ?";
			MainMenu.prepareStatement();
			MainMenu.stmt.setInt(1, Integer.parseInt(trans_idString));
			if (rdio_trans_schuld.isSelected()) {
				MainMenu.stmt.setDouble(2, (double) sp_info_betrag.getValue() * (-1));
			}
			else {
				MainMenu.stmt.setDouble(2, (double) sp_info_betrag.getValue());
			}
			MainMenu.stmt.setString(3, txt_trans_info_desc.getText());
			MainMenu.stmt.setDate(4, SQLDate);
			MainMenu.stmt.setInt(5, SelectedID);
			MainMenu.stmt.execute();
			infoBox(this, "Die Transaktion Nummer " + SelectedID + " wurde erfolgreich geändert!", "Transaktion geändert!");
		} catch (SQLException | ParseException ex) {
			errorBox(this, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
		}
		
    }//GEN-LAST:event_btn_trans_ändernActionPerformed

    private void btn_trans_löschenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_trans_löschenActionPerformed
		try {
			MainMenu.SQL = "DELETE FROM patchowe_transaktionen WHERE id = ?";
			MainMenu.prepareStatement();
			MainMenu.stmt.setInt(1, SelectedID);
			MainMenu.stmt.execute();
			infoBox(this, "Die Transaktion Nummer " + SelectedID + " wurde erfolgreich gelöscht!", "Transaktion gelöscht!");
		} catch (SQLException ex) {
			Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
		}
    }//GEN-LAST:event_btn_trans_löschenActionPerformed

    private void dlg_transaktionenWindowLostFocus(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_dlg_transaktionenWindowLostFocus
        index_trans_names = cbox_trans_freund.getSelectedIndex();
    }//GEN-LAST:event_dlg_transaktionenWindowLostFocus

    private void btn_firststart_beendenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_firststart_beendenActionPerformed
        Runtime.getRuntime().exit(1);
    }//GEN-LAST:event_btn_firststart_beendenActionPerformed

    private void mitem_optionenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitem_optionenActionPerformed
        dlg_optionen.pack();
		dlg_optionen.setLocationRelativeTo(null);
		dlg_optionen.setVisible(true);
    }//GEN-LAST:event_mitem_optionenActionPerformed

    private void btn_optionen_speichern1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_optionen_speichern1ActionPerformed
        Host = txt_optionen_mysql_host.getText();
		DB_Name = txt_optionen_mysql_dbname.getText();
		UserName = txt_optionen_mysql_uname.getText();
		Password = String.valueOf(pswd_optionen_mysql_passwort.getPassword());
				
		savevariable("Host", Host.toString());
		savevariable("DB_Name", DB_Name.toString());
		savevariable("UserName", UserName.toString());
		savevariable("Password", Password.toString());
		
		if (rdio_optionen_menü_showtoolbox.isSelected()) {
			MenuStyle = 1;
		}
		else if (rdio_optionen_menü_showmenü.isSelected()) {
			MenuStyle = 2;
		}
		else {
			MenuStyle = 3;
		}
		
		savevariable("MenuStyle", String.valueOf(MenuStyle));
		infoBox(this, "Die Optionen wurden übernommen!", "Optionen gespeichert!");
		dlg_optionen.dispose();
    }//GEN-LAST:event_btn_optionen_speichern1ActionPerformed

    private void btn_firststart_speichernActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_firststart_speichernActionPerformed
        Host = txt_firststart_host.getText();
		DB_Name = txt_firststart_dbname.getText();
		UserName = txt_firststart_uname.getText();
		Password = String.valueOf(pswd_firststart_passwort.getPassword());
		
		savevariable("Host", Host.toString());
		savevariable("DB_Name", DB_Name.toString());
		savevariable("UserName", UserName.toString());
		savevariable("Password", Password.toString());
		
		parseHostURL();
		
		MenuStyle = 1;
		savevariable("MenuStyle", String.valueOf(MenuStyle));
		
		FirstStart = false;
		savevariable("FirstStart", FirstStart.toString());
		infoBox(this, "Die Konfiguration wurde erfolgreich gespeichert!", "Konfiguration gespeichert!");
		dlg_firststart.dispose();
    }//GEN-LAST:event_btn_firststart_speichernActionPerformed

    private void dlg_optionenWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_dlg_optionenWindowActivated
        if (getvariable("Host") != null) {
			Host = p.getProperty("Host");
		}
		if (getvariable("DB_Name") != null) {
			DB_Name = p.getProperty("DB_Name");
		}
		if (getvariable("UserName") != null) {
			UserName = p.getProperty("UserName");
		}
		if (getvariable("Password") != null) {
			Password = p.getProperty("Password");
		}
		if (getvariable("MenuStyle") != null) {
			MenuStyle = Integer.parseInt(p.getProperty("MenuStyle"));
		}
		txt_optionen_mysql_host.setText(Host);
		txt_optionen_mysql_dbname.setText(DB_Name);
		txt_optionen_mysql_uname.setText(UserName);
		pswd_optionen_mysql_passwort.setText(Password);
		
		if (MenuStyle == 1) {
			rdio_optionen_menü_showtoolbox.setSelected(true);
		}
		else if (MenuStyle == 2) {
			rdio_optionen_menü_showmenü.setSelected(true);
		}
		else {
			rdio_optionen_menü_showboth.setSelected(true);
		}
		
    }//GEN-LAST:event_dlg_optionenWindowActivated

    private void btn_optionen_abbrechen1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_optionen_abbrechen1ActionPerformed
        dlg_optionen.dispose();
    }//GEN-LAST:event_btn_optionen_abbrechen1ActionPerformed

    private void mitem_geldbestandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitem_geldbestandActionPerformed
        mymoney.setVisible(true);
    }//GEN-LAST:event_mitem_geldbestandActionPerformed

    private void mitem_virtuellezahlungActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitem_virtuellezahlungActionPerformed
        vpay.setVisible(true);
    }//GEN-LAST:event_mitem_virtuellezahlungActionPerformed

    private void tbar_btn_addfriendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbar_btn_addfriendActionPerformed
        NewFriend newfriend = new NewFriend();
        newfriend.pack();
		newfriend.setVisible(true);
    }//GEN-LAST:event_tbar_btn_addfriendActionPerformed

    private void tbar_btn_removefriendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbar_btn_removefriendActionPerformed
        dlg_personenlöschen.pack();
		dlg_personenlöschen.setLocationRelativeTo(null);
		dlg_personenlöschen.setVisible(true);
    }//GEN-LAST:event_tbar_btn_removefriendActionPerformed

    private void tbar_btn_mymoneyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbar_btn_mymoneyActionPerformed
        mymoney.setVisible(true);
    }//GEN-LAST:event_tbar_btn_mymoneyActionPerformed

    private void tbar_btn_virtualpayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbar_btn_virtualpayActionPerformed
        vpay.setVisible(true);
    }//GEN-LAST:event_tbar_btn_virtualpayActionPerformed

    private void tbar_btn_optionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbar_btn_optionsActionPerformed
        dlg_optionen.pack();
		dlg_optionen.setLocationRelativeTo(null);
		dlg_optionen.setVisible(true);
    }//GEN-LAST:event_tbar_btn_optionsActionPerformed

    private void mitem_überActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mitem_überActionPerformed
        dlg_about.pack();
		dlg_about.setLocationRelativeTo(null);
		dlg_about.setVisible(true);
    }//GEN-LAST:event_mitem_überActionPerformed

    private void btn_about_schliessenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_about_schliessenActionPerformed
        dlg_about.dispose();
    }//GEN-LAST:event_btn_about_schliessenActionPerformed

    private void tbar_btn_aboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbar_btn_aboutActionPerformed
		dlg_about.pack();
		dlg_about.setLocationRelativeTo(null);
		dlg_about.setVisible(true);
    }//GEN-LAST:event_tbar_btn_aboutActionPerformed

    private void dlg_aboutWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_dlg_aboutWindowActivated
		lbl_about_info.setText("<html>Mit PatchOwe können Sie Ihre gesamten Schulden verwalten und einsehen, Rückzahlungen erstellen.<br><br>" +
"Zusätzlich können Sie Ihren Geldbestand eingeben, um dann Ihren Gesamtgeldbestand nach Begleichung aller Schulden zu sehen. Auch möglich sind sogenannte \"virtuelle Zahlungen\". Sie können Produkte mit Ihrem Preis eintragen, um dann berechnen zu lassen, wie oft Sie ein Produkt kaufen können und wieviel Geld Sie danach haben werden.<br>" +
"In zukünftigen Versionen können Sie sogar sogenannte \"Einkaufslisten\" erstellen und abspeichern.</html>");
		
		try {
			URL url = new URL("http://averi.at/programme/patchowe/version.html");      
			BufferedReader r = new BufferedReader(new InputStreamReader( url.openStream()));      
			String line = null;
			line = r.readLine();
			NewestVersion = line;
			lbl_about_newestversion.setText("<html>Neueste Version: <b>" + line + "</b></html>");
			if (PatchOweVersion.equals(NewestVersion)) {
				lbl_about_version.setText("<html>Sie verwenden PatchOwe-Version <font color=\"green\">" + PatchOweVersion + "</font></html>");
			}
			else {
				lbl_about_version.setText("<html>Sie verwenden PatchOwe-Version <font color=\"red\">" + PatchOweVersion + 
						" - PatchOwe Update verfügbar!</font></html>");
			}
			
		} catch (MalformedURLException ex) {
			Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
		}
    }//GEN-LAST:event_dlg_aboutWindowActivated

    private void dlg_aboutWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_dlg_aboutWindowClosed
        lbl_about_info.setText("Information...");
    }//GEN-LAST:event_dlg_aboutWindowClosed

	/**
	 *
	 * @param cp
	 * @param message
	 * @param title
	 */
	public static void infoBox (Component cp, String message, String title) {
	JOptionPane.showMessageDialog(cp, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
	
    /**
     *
     * @param message
     * @param title
     */
    protected static void errorBox (Component cp, String message, String title) {
	JOptionPane.showMessageDialog(cp, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    protected void updateinfo() {
		try {
			if (connected) {
				lbl_database.setText("<html>Datenbank-Verbindung: <font color = \"green\">OK</font>");
                SQL = "SELECT sum(betrag) FROM patchowe_transaktionen";
                MainMenu.prepareStatement();
                rs = stmt.executeQuery();
                rs.next();
                summegesamt = rs.getDouble(1);
				SQL = "SELECT COUNT(*) FROM patchowe_transaktionen";
				MainMenu.prepareStatement();
				rs = stmt.executeQuery();
				rs.next();
				anzahlgesamt = rs.getInt(1);
			}
			else {
				lbl_database.setText("<html>Datenbank-Verbindung: <font color = \"red\">Fehler/Nicht verbunden</font>");
			}
        } catch (Exception ex) {
            errorBox(this, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
        }
	
	if (summegesamt>=0) {
	    lbl_info_balance.setText("<html>Du bekommst noch <font color = \"green\">" + String.valueOf(f.format(summegesamt)) + "</font>");
	}
	else {
	    lbl_info_balance.setText("<html>Du schuldest noch <font color = \"red\">" + String.valueOf(f.format(-summegesamt)) + "</font>");
	}
	if (anzahlgesamt>0) {
		lbl_info_anzahl.setText("<html>Du hast noch <b>" + anzahlgesamt + "</b> offene Transaktionen.</html>");
	}
	else {
		lbl_info_anzahl.setText("<html>Du hast <b>keine</b> offenen Transaktionen.</html>");
	}
    }
    
    private void tbl_cbox_names_refresh() {
	try {
	    SQL = "SELECT vorname, nachname FROM patchowe_personen";
	    MainMenu.prepareStatement();
	    rs_tabelle_names = stmt.executeQuery();
	    if (cbox_tabelle_namen.getItemCount() >= 1) {
			cbox_tabelle_namen.removeAllItems();
		}
		cbox_tabelle_namen.addItem("Alle");
	    while (rs_tabelle_names.next()) {
			String result = rs_tabelle_names.getString("vorname");
			String result2 = rs_tabelle_names.getString("nachname");
			if (result != null) {
				result = result.trim();
			}
			if (result2 != null) {
				result2 = result2.trim();
			}
			cbox_tabelle_namen.addItem(result + " " + result2);
			//cbox_namen.addItem(rs_tabelle_names.getString("vorname") + " " + rs_tabelle_names.getString("nachname"));
	    }
		stmt.closeOnCompletion();
		if (si_tabelle_cbox_namen >= 0) {
				cbox_tabelle_namen.setSelectedIndex(si_tabelle_cbox_namen);
		}
	} catch (SQLException ex) {
	    errorBox(this, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
	}
    }
    
    private void tbl_info_forname() {
	    try {

		SQL = "select sum(numbers) FROM ("
			+ " SELECT patchowe_transaktionen.betrag as numbers"
			+ " FROM patchowe_transaktionen, patchowe_personen"
			+ " WHERE patchowe_transaktionen.pid = patchowe_personen.id"
			+ " AND patchowe_personen.vorname = ?"
			+ " AND patchowe_personen.nachname = ?"
			+ " AND betrag < 0 ) src";
		MainMenu.prepareStatement();
		stmt.setString(1, splitNames[0]);
		stmt.setString(2, splitNames[1]);
		rs = stmt.executeQuery();
		rs.next();
		summeschulden = rs.getDouble(1);
		
		SQL = "select sum(numbers) FROM ("
			+ " SELECT patchowe_transaktionen.betrag as numbers"
			+ " FROM patchowe_transaktionen, patchowe_personen"
			+ " WHERE patchowe_transaktionen.pid = patchowe_personen.id"
			+ " AND patchowe_personen.vorname = ?"
			+ " AND patchowe_personen.nachname = ?"
			+ " AND betrag >= 0 ) src";
		MainMenu.prepareStatement();
		stmt.setString(1, splitNames[0]);
		stmt.setString(2, splitNames[1]);
		rs = stmt.executeQuery();
		rs.next();
		summerückzahlungen = rs.getDouble(1);
	    
		tbl_setinfotexts();
	    
	    } catch (SQLException ex) {
		errorBox(this, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
	    }
    }

    private void tbl_setinfotexts() {
    
	    summegesamt = summeschulden + summerückzahlungen;
	    
	    txt_tabelle_schulden.setText(String.valueOf(f.format(-summeschulden)));
	    txt_tabelle_verliehen.setText(String.valueOf(f.format(summerückzahlungen)));
	    txt_tabelle_gesamt.setText(String.valueOf(f.format(summegesamt)));
	    
	    if (summegesamt >= 0) {
		txt_tabelle_gesamt.setForeground(new Color(51,153,0));
	    }
	    else {
		txt_tabelle_gesamt.setForeground(new Color(255,0,0));
	    }
	
}
    
    private void fillTable() {
	try {
	    DefaultTableModel model = (DefaultTableModel) tbl_tabelle_daten.getModel();
	    SimpleDateFormat d = new SimpleDateFormat("dd.MMMM.yyyy");
	    
		    if (model.getRowCount() > 0) {
			for (int i = model.getRowCount() - 1; i > -1; i--) {
			    model.removeRow(i);
			}
		    }
		    while (rs_tabelle_fill.next()) {
		    if (rs_tabelle_fill.getDouble("betrag") >= 0) {
			model.addRow(new Object[]{rs_tabelle_fill.getString("vorname") + " "
			    + rs_tabelle_fill.getString("nachname"), d.format(rs_tabelle_fill.getDate("datum")), rs_tabelle_fill.getString("beschreibung"),
			    "<html><font color=\"green\">" + f.format(rs_tabelle_fill.getDouble("betrag")) + "</font></html>"});
		    }
		    else {
			model.addRow(new Object[]{rs_tabelle_fill.getString("vorname") + " "
			    + rs_tabelle_fill.getString("nachname"), d.format(rs_tabelle_fill.getDate("datum")), rs_tabelle_fill.getString("beschreibung"),
			    "<html><font color=\"red\">" + f.format(rs_tabelle_fill.getDouble("betrag")) + "</font></html>"});
		    }
		    
		}
	} catch (SQLException ex) {
	    errorBox(this, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
	}
    }
    
    private void fillTableWithName() {
	try {
				
		String ausgewählt;
		ausgewählt = cbox_tabelle_namen.getSelectedItem().toString();
		System.out.println(ausgewählt);
		splitNames = ausgewählt.split(" ");
		
		SQL = "SELECT patchowe_personen.vorname, patchowe_personen.nachname,"
			+ " patchowe_transaktionen.datum, patchowe_transaktionen.betrag,"
			+ " patchowe_transaktionen.beschreibung"
			+ " FROM patchowe_personen, patchowe_transaktionen"
			+ " WHERE patchowe_personen.id = patchowe_transaktionen.pid"
			+ " AND patchowe_personen.vorname = ?"
			+ " AND patchowe_personen.nachname = ?";
		MainMenu.prepareStatement();
		stmt.setString(1, splitNames[0]);
		stmt.setString(2, splitNames[1]);
		rs_tabelle_fill = stmt.executeQuery();
		fillTable();
		
		
	    } catch (SQLException ex) {
		errorBox(this, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
	    }
    }
	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[]) {
		/* Set the Nimbus look and feel */
		//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
		 * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
		 */
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(MainMenu.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		//</editor-fold>
		
		
		splashinit();
		appinit();
		
		if (mySplash != null)
			mySplash.close();
		
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new MainMenu().setVisible(true);
			}
		});
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_about_downloadnewestversion;
    private javax.swing.JButton btn_about_schliessen;
    private javax.swing.JButton btn_deleteperson_abbrechen;
    private javax.swing.JButton btn_deleteperson_löschen;
    private javax.swing.JButton btn_firststart_beenden;
    private javax.swing.JButton btn_firststart_speichern;
    private javax.swing.JButton btn_looktransactions;
    private javax.swing.JButton btn_neuereintrag;
    private javax.swing.JButton btn_neuerückzahlung;
    private javax.swing.JButton btn_optionen_abbrechen1;
    private javax.swing.JButton btn_optionen_speichern1;
    private javax.swing.JButton btn_tabelle_anzeigen;
    private javax.swing.JButton btn_trans_abbrechen;
    private javax.swing.JButton btn_trans_löschen;
    private javax.swing.JButton btn_trans_ändern;
    private javax.swing.JComboBox<String> cbox_deleteperson_friends;
    private javax.swing.JComboBox<String> cbox_tabelle_namen;
    private javax.swing.JComboBox<String> cbox_trans_freund;
    private javax.swing.JComboBox<String> cbox_trans_info_names;
    private javax.swing.JComboBox<String> cbox_trans_trans;
    private javax.swing.JDialog dlg_about;
    protected static javax.swing.JDialog dlg_firststart;
    private javax.swing.JDialog dlg_optionen;
    private javax.swing.JDialog dlg_personenlöschen;
    private javax.swing.JDialog dlg_transaktionen;
    private javax.swing.JFrame frm_tabelle;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JLabel lbl_about_info;
    private javax.swing.JLabel lbl_about_newestversion;
    private javax.swing.JLabel lbl_about_version;
    private javax.swing.JLabel lbl_database;
    private javax.swing.JLabel lbl_firststart_dbname;
    private javax.swing.JLabel lbl_firststart_host;
    private javax.swing.JLabel lbl_firststart_pass;
    private javax.swing.JLabel lbl_firststart_uname;
    private javax.swing.JLabel lbl_freund;
    private javax.swing.JLabel lbl_info_anzahl;
    protected static javax.swing.JLabel lbl_info_balance;
    private javax.swing.JLabel lbl_optionen_mysql_dbname;
    private javax.swing.JLabel lbl_optionen_mysql_host;
    private javax.swing.JLabel lbl_optionen_mysql_pass;
    private javax.swing.JLabel lbl_optionen_mysql_uname;
    private javax.swing.JLabel lbl_tabelle_gesamt;
    private javax.swing.JLabel lbl_tabelle_schulden;
    private javax.swing.JLabel lbl_tabelle_verliehen;
    private javax.swing.JLabel lbl_trans_info_betrag;
    private javax.swing.JLabel lbl_trans_info_datum;
    private javax.swing.JLabel lbl_trans_info_desc;
    private javax.swing.JLabel lbl_trans_info_name;
    private javax.swing.JMenu m_datei;
    private javax.swing.JMenu m_hilfe;
    private javax.swing.JMenu m_löschen;
    private javax.swing.JMenu m_neu;
    private javax.swing.JMenu m_tools;
    private javax.swing.JMenu m_ändern;
    private javax.swing.JMenuBar mbar_home;
    private javax.swing.JMenuItem mitem_allesanzeigen;
    private javax.swing.JMenuItem mitem_alleslöschen;
    private javax.swing.JMenuItem mitem_atransaktionenlöschen;
    private javax.swing.JMenuItem mitem_beenden;
    private javax.swing.JMenuItem mitem_btransaktionenlöschen;
    private javax.swing.JMenuItem mitem_geldbestand;
    private javax.swing.JMenuItem mitem_hilfe;
    private javax.swing.JMenuItem mitem_neueperson;
    private javax.swing.JMenuItem mitem_optionen;
    private javax.swing.JMenuItem mitem_personenlöschen;
    private javax.swing.JMenuItem mitem_transaktionenändernlöschen;
    private javax.swing.JMenuItem mitem_virtuellezahlung;
    private javax.swing.JMenuItem mitem_über;
    private javax.swing.JPanel pnl_aktionen;
    private javax.swing.JPanel pnl_firststart_verbindung;
    private javax.swing.JPanel pnl_information;
    private javax.swing.JPanel pnl_optionen_menü;
    private javax.swing.JPanel pnl_optionen_mysql;
    private javax.swing.JPanel pnl_trans;
    private javax.swing.JPasswordField pswd_firststart_passwort;
    private javax.swing.JPasswordField pswd_optionen_mysql_passwort;
    private javax.swing.JPopupMenu pup_daten;
    private javax.swing.JRadioButton rdio_optionen_menü_showboth;
    private javax.swing.JRadioButton rdio_optionen_menü_showmenü;
    private javax.swing.JRadioButton rdio_optionen_menü_showtoolbox;
    private javax.swing.JRadioButton rdio_trans_schuld;
    private javax.swing.JRadioButton rdio_trans_verliehen;
    private javax.swing.ButtonGroup rdiogrp_optionen_menü;
    private javax.swing.ButtonGroup rdiogrp_trans;
    private javax.swing.JScrollPane scrl_datenscrollbar;
    private javax.swing.JSpinner sp_info_betrag;
    private javax.swing.JButton tbar_btn_about;
    private javax.swing.JButton tbar_btn_addfriend;
    private javax.swing.JButton tbar_btn_help;
    private javax.swing.JButton tbar_btn_mymoney;
    private javax.swing.JButton tbar_btn_options;
    private javax.swing.JButton tbar_btn_removefriend;
    private javax.swing.JButton tbar_btn_virtualpay;
    private javax.swing.JToolBar tbar_home;
    private javax.swing.JTable tbl_tabelle_daten;
    private javax.swing.JTextField txt_firststart_dbname;
    private javax.swing.JTextField txt_firststart_host;
    private javax.swing.JTextField txt_firststart_uname;
    private javax.swing.JTextField txt_optionen_mysql_dbname;
    private javax.swing.JTextField txt_optionen_mysql_host;
    private javax.swing.JTextField txt_optionen_mysql_uname;
    private javax.swing.JTextField txt_tabelle_gesamt;
    private javax.swing.JTextField txt_tabelle_schulden;
    private javax.swing.JTextField txt_tabelle_verliehen;
    private javax.swing.JTextField txt_trans_info_datum;
    private javax.swing.JTextField txt_trans_info_desc;
    // End of variables declaration//GEN-END:variables

	protected static void prepareStatement() {
		try {
	    	stmt = con.prepareStatement(SQL);
		} catch (SQLException ex) {
			errorBox(null, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
		}
    }

	private void transrefresh() {
		try {
			String sel;
			
			sel = cbox_trans_freund.getSelectedItem().toString();
			
			String[] splitResult = sel.split(" ");
			
			String idString = null;
			
			MainMenu.SQL = "SELECT id FROM patchowe_personen WHERE vorname = ? AND nachname = ?";
			MainMenu.prepareStatement();
			MainMenu.stmt.setString(1, splitResult[0]);
			MainMenu.stmt.setString(2, splitResult[1]);
			rs_trans_trans = MainMenu.stmt.executeQuery();
			
			if (rs_trans_trans.next()) {
				idString=rs_trans_trans.getString(1);
			}
			
			
			MainMenu.SQL = "SELECT id, beschreibung, betrag "
					+ "FROM patchowe_transaktionen WHERE pid = ?";
			MainMenu.prepareStatement();
			MainMenu.stmt.setInt(1, Integer.parseInt(idString));
			rs_trans_trans = MainMenu.stmt.executeQuery();
			cbox_trans_trans.removeAllItems();
			while (rs_trans_trans.next()) {
				cbox_trans_trans.addItem(rs_trans_trans.getInt("id") + " - " + rs_trans_trans.getString("beschreibung")
						+ " - " + f.format(rs_trans_trans.getDouble("betrag")));
			}
			if (cbox_trans_freund.getSelectedIndex()>=1) {
				cbox_trans_info_names.setSelectedIndex(cbox_trans_freund.getSelectedIndex());
			}
			//if (si_friends >= 0) {
				//cbox_freund.setSelectedIndex(si_friends);
			//}
		} catch (SQLException ex) {
			MainMenu.errorBox(this, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
		}
	}

	private void trans_namesrefresh() {
		try {
			MainMenu.SQL = "SELECT vorname, nachname FROM patchowe_personen";
			MainMenu.prepareStatement();
			MainMenu.rs_trans_names = MainMenu.stmt.executeQuery();
			cbox_trans_freund.removeAllItems();
			cbox_trans_info_names.removeAllItems();
			while (MainMenu.rs_trans_names.next()) {
				cbox_trans_freund.addItem(MainMenu.rs_trans_names.getString("vorname") + " " + MainMenu.rs_trans_names.getString("nachname"));
				cbox_trans_info_names.addItem(MainMenu.rs_trans_names.getString("vorname") + " " + MainMenu.rs_trans_names.getString("nachname"));
			}
			//if (si_friends >= 0) {
				//cbox_freund.setSelectedIndex(si_friends);
			//}
		} catch (SQLException ex) {
			MainMenu.errorBox(this, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
		}
	}

	private void trans_inforefresh() {
		try {
			String sel;
			sel = cbox_trans_trans.getSelectedItem().toString();
			String[] splitResult = sel.split(" - ");
			double Betrag;
			SelectedID = Integer.parseInt(splitResult[0]);
			
			MainMenu.SQL = "SELECT betrag, beschreibung, datum FROM patchowe_transaktionen WHERE id = ?";
			MainMenu.prepareStatement();
			MainMenu.stmt.setInt(1, SelectedID);
			rs_info = MainMenu.stmt.executeQuery();
			if (rs_info.next()) {
				Betrag = rs_info.getDouble("betrag");
				
				if (Betrag >= 0) {
					rdio_trans_verliehen.setSelected(true);
				}
				else {
					rdio_trans_schuld.setSelected(true);
				}
				
				
				sp_info_betrag.setValue(Math.abs(Betrag));
				txt_trans_info_desc.setText(rs_info.getString("beschreibung"));
				txt_trans_info_datum.setText(d.format(rs_info.getDate("datum")));
			}
			
		} catch (SQLException ex) {
			Logger.getLogger(NewRepayment.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private static void createConfigFile() {
		try {
			p = new Properties();
			ConfigFile = new File("config.po");
			ConfigFile.createNewFile();
			
		} catch (IOException ex) {
			MainMenu.errorBox(null, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
		}
	}

	protected static void savevariable(String name, String wert) {
		try {
			MainMenu.p.load(new FileInputStream(ConfigFile));
			MainMenu.p.setProperty(name, wert);
			MainMenu.p.store(new FileOutputStream(ConfigFile), "config.po");
		} catch (Exception ex) {
			MainMenu.errorBox(null, "Ein Fehler beim Speichern der Variable " + name + " ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
		}
	}

	protected static String getvariable(String name) {
		String result = null;
		try {
			p.load(new FileInputStream(ConfigFile));
			result = p.getProperty(name);
			
		} catch (IOException ex) {
			MainMenu.errorBox(null, "Ein Fehler beim Laden der Variable " + name + " ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
		}
		return result;
	}

	private static void createcon() {
		try {
			MainMenu.parseHostURL();
			if (getvariable("UserName") != null) {
				MainMenu.UserName = p.getProperty("UserName");
			}
			if (getvariable("Password") != null) {
				MainMenu.Password = p.getProperty("Password");
			}
			MainMenu.con = DriverManager.getConnection( MainMenu.HostURL, MainMenu.UserName, MainMenu.Password );
		} catch (SQLException ex) {
			MainMenu.errorBox(null, "Verbindung mit der Datenbank konnte nicht hergestellt werden.\n"
					+ "Möglicherweise besteht keine Internetverbindung.\n"
					+ "Das Programm wird jetzt beendet.\n" + ex.getMessage(), "Verbindung konnte nicht hergestellt werden");
		}
	}

	private static void parseHostURL() {
		if (getvariable("Host") != null) {
			MainMenu.Host = p.getProperty("Host");
		}
		if (getvariable("DB_Name") != null) {
			MainMenu.DB_Name = p.getProperty("DB_Name");
		}
		MainMenu.HostURL = "jdbc:mysql://" + Host + ":3306/" + DB_Name;
	}

	private void tbl_showall() {
		try {
                SQL = "SELECT patchowe_personen.vorname, patchowe_personen.nachname,"
                + " patchowe_transaktionen.datum, patchowe_transaktionen.betrag,"
                + " patchowe_transaktionen.beschreibung"
                + " FROM patchowe_personen, patchowe_transaktionen"
                + " WHERE patchowe_personen.id = patchowe_transaktionen.pid";
				
				//cbox_tabelle_namen.removeAllItems();
                MainMenu.prepareStatement();
                rs_tabelle_fill = stmt.executeQuery();
                fillTable();

                SQL = "SELECT sum(betrag) FROM patchowe_transaktionen WHERE betrag < 0";
                MainMenu.prepareStatement();
                rs = stmt.executeQuery();
                rs.next();
                summeschulden = rs.getDouble(1);

                SQL = "SELECT sum(betrag) FROM patchowe_transaktionen WHERE betrag >= 0";
                MainMenu.prepareStatement();
                rs = stmt.executeQuery();
                rs.next();
                summerückzahlungen = rs.getDouble(1);

                tbl_setinfotexts();

            } catch (Exception ex) {
                errorBox(this, "Ein Fehler ist aufgetreten.\nFehlermeldung:\n'" + ex.getMessage() + "'", "Fehler!");
            }
	}

	private static void tblcheck() {
		try {
			DatabaseMetaData dbm = con.getMetaData();
			ResultSet tables;
			tables = dbm.getTables(null, null, "patchowe_personen", null);
			if (tables.next()) {
			splashText("Tabelle patchowe_personen exisitert!");
			}
			else {
			// Table existiert nicht
				splashText("Erstelle Tabelle patchowe_personen!");
				SQL = "CREATE TABLE patchowe_personen (id INT AUTO_INCREMENT PRIMARY KEY, vorname VARCHAR(50) NOT NULL, "
					+ "nachname VARCHAR(50) NOT NULL, kommentar VARCHAR(50))";
				MainMenu.prepareStatement();
				MainMenu.stmt.execute();
			}
			
			tables = dbm.getTables(null, null, "patchowe_transaktionen", null);
			if (tables.next()) {
			splashText("Tabelle patchowe_transaktionen exisitert!");
			}
			else {
			// Table existiert nicht
				splashText("Erstelle Tabelle patchowe_transaktionen!");
				SQL = "CREATE TABLE patchowe_transaktionen (id INT AUTO_INCREMENT PRIMARY KEY, pid INT NOT NULL, datum DATE NOT NULL, "
					+ "betrag DOUBLE NOT NULL, beschreibung VARCHAR(50))";
				MainMenu.prepareStatement();
				MainMenu.stmt.execute();
			}
			
			tables = dbm.getTables(null, null, "patchowe_produkte", null);
			if (tables.next()) {
			splashText("Tabelle patchowe_produkte exisitert!");
			}
			else {
			// Table existiert nicht
				splashText("Erstelle Tabelle patchowe_produkte!");
				SQL = "CREATE TABLE patchowe_produkte (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL, wert DOUBLE NOT NULL, "
					+ "kommentar VARCHAR(50))";
				MainMenu.prepareStatement();
				MainMenu.stmt.execute();
			}
		} catch (SQLException ex) {
			Logger.getLogger(MainMenu.class.getName()).log(Level.SEVERE, null, ex);
		}
		
	}

	private void refreshMenuStyle() {
		if (ConfigFile != null) {
			if (getvariable("MenuStyle") != null) {
				MenuStyle = Integer.parseInt(p.getProperty("MenuStyle"));
			}
			
			if (MenuStyle == 1) {
				//mbar_home.setMinimumSize(new Dimension(105, 0));
				//mbar_home.setPreferredSize(new Dimension(105, 0));
				//tbar_home.setMinimumSize(new Dimension(235,35));
				//tbar_home.setPreferredSize(new Dimension(235,35));
				mbar_home.setVisible(false);
				tbar_home.setVisible(true);
			}
			else if (MenuStyle == 2) {
				//mbar_home.setMinimumSize(new Dimension(105, 21));
				//mbar_home.setPreferredSize(new Dimension(105, 21));
				//tbar_home.setMinimumSize(new Dimension(235,0));
				//tbar_home.setPreferredSize(new Dimension(235,0));
				mbar_home.setVisible(true);
				tbar_home.setVisible(false);
			}
			else {
				//mbar_home.setMinimumSize(new Dimension(105, 21));
				//mbar_home.setPreferredSize(new Dimension(105, 21));
				//tbar_home.setMinimumSize(new Dimension(235,35));
				//tbar_home.setPreferredSize(new Dimension(235,35));
				mbar_home.setVisible(true);
				tbar_home.setVisible(true);
			}
			this.pack();
		}
		
	}


}
