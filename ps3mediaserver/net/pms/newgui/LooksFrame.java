/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.newgui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.gui.IFrame;
import net.pms.io.WindowsNamedPipe;
import net.pms.newgui.update.AutoUpdateDialog;
import net.pms.update.AutoUpdater;
import net.pms.util.PMSUtil;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.sun.jna.Platform;

public class LooksFrame extends JFrame implements IFrame, Observer {
	
	private final AutoUpdater autoUpdater;
	private final PmsConfiguration configuration;
	public static final String START_SERVICE = "start.service"; //$NON-NLS-1$

	private static final long serialVersionUID = 8723727186288427690L;
	public TracesTab getTt() {
		return tt;
	}
	
	private FoldTab ft;

	public FoldTab getFt() {
		return ft;
	}

	private StatusTab st;
	private TracesTab tt;
	private TrTab2 tr;
	public TrTab2 getTr() {
		return tr;
	}

	private NetworkTab nt; 
	private AbstractButton reload ;
	
	public AbstractButton getReload() {
		return reload;
	}
	 
	 private JLabel status;

	protected static final Dimension PREFERRED_SIZE = new Dimension(1000, 750);
	// https://code.google.com/p/ps3mediaserver/issues/detail?id=949
	protected static final Dimension MINIMUM_SIZE = new Dimension(800, 480);


	/**
	 * Constructs a <code>DemoFrame</code>, configures the UI,
	 * and builds the content.
	 */
	public LooksFrame(AutoUpdater autoUpdater, PmsConfiguration configuration) {
		this.autoUpdater = autoUpdater;
		this.configuration = configuration;
		assertThat(this.autoUpdater, notNullValue());
		assertThat(this.configuration, notNullValue());
		autoUpdater.addObserver(this);
		update(autoUpdater, null);
		Options.setDefaultIconSize(new Dimension(18, 18));

		Options.setUseNarrowButtons(true);

		// Global options
		Options.setTabIconsEnabled(true);
		UIManager.put(Options.POPUP_DROP_SHADOW_ENABLED_KEY, null);

		// Swing Settings
		LookAndFeel selectedLaf = null;
		if (PMS.get().isWindows()) {
			try {
				selectedLaf = (LookAndFeel) Class.forName("com.jgoodies.looks.windows.WindowsLookAndFeel").newInstance(); //$NON-NLS-1$
				//selectedLaf = (LookAndFeel) Class.forName("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel").newInstance(); //$NON-NLS-1$
			} catch (Exception e) {
				selectedLaf = new PlasticLookAndFeel();
			}
		}
		else if (System.getProperty("nativelook") == null && !Platform.isMac()) //$NON-NLS-1$
			selectedLaf = new PlasticLookAndFeel();
		else {
			try {
				String systemClassName = UIManager.getSystemLookAndFeelClassName();
				// workaround for gnome
				try {
					String gtkLAF = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel"; //$NON-NLS-1$
					Class.forName(gtkLAF);
					if (systemClassName.equals("javax.swing.plaf.metal.MetalLookAndFeel")) //$NON-NLS-1$
						systemClassName = gtkLAF;
				} catch (ClassNotFoundException ce) {}
				
				PMS.info("Choosing java look and feel: " + systemClassName); //$NON-NLS-1$
				UIManager.setLookAndFeel(systemClassName);
			} catch (Exception e1) {
				selectedLaf = new PlasticLookAndFeel();
				PMS.error("Error while setting native look and feel: ", e1); //$NON-NLS-1$
			}
		}
		
		if (selectedLaf instanceof PlasticLookAndFeel) {
			PlasticLookAndFeel.setPlasticTheme(PlasticLookAndFeel.createMyDefaultTheme());
			PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_DEFAULT_VALUE);
			PlasticLookAndFeel.setHighContrastFocusColorsEnabled(false);
		} else if (selectedLaf != null && selectedLaf.getClass() == MetalLookAndFeel.class) {
			MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
		}

		// Work around caching in MetalRadioButtonUI
		JRadioButton radio = new JRadioButton();
		radio.getUI().uninstallUI(radio);
		JCheckBox checkBox = new JCheckBox();
		checkBox.getUI().uninstallUI(checkBox);

		if (selectedLaf != null) {
			try {
				UIManager.setLookAndFeel(selectedLaf);
			} catch (Exception e) {
				System.out.println("Can't change L&F: " + e); //$NON-NLS-1$
			}
		}
		
		// http://propedit.sourceforge.jp/propertieseditor.jnlp
		
		Font sf = null;
	
		// Set an unicode font for testing exotics languages (japanese)
		if (PMS.getConfiguration().getLanguage() != null && (PMS.getConfiguration().getLanguage().equals("ja") || PMS.getConfiguration().getLanguage().startsWith("zh"))) //$NON-NLS-1$ //$NON-NLS-2$
				sf = new Font("Serif", Font.PLAIN, 12); //$NON-NLS-1$
		
		if (sf != null) {
			UIManager.put("Button.font",sf);  //$NON-NLS-1$
			UIManager.put("ToggleButton.font",sf);  //$NON-NLS-1$
			UIManager.put("RadioButton.font",sf);  //$NON-NLS-1$
			UIManager.put("CheckBox.font",sf);  //$NON-NLS-1$
			UIManager.put("ColorChooser.font",sf);  //$NON-NLS-1$
			UIManager.put("ToggleButton.font",sf);  //$NON-NLS-1$
			UIManager.put("ComboBox.font",sf);  //$NON-NLS-1$
			UIManager.put("ComboBoxItem.font",sf);  //$NON-NLS-1$
			UIManager.put("InternalFrame.titleFont",sf);  //$NON-NLS-1$
			UIManager.put("Label.font",sf);  //$NON-NLS-1$
			UIManager.put("List.font",sf);  //$NON-NLS-1$
			UIManager.put("MenuBar.font",sf);  //$NON-NLS-1$
			UIManager.put("Menu.font",sf);  //$NON-NLS-1$
			UIManager.put("MenuItem.font",sf);  //$NON-NLS-1$
			UIManager.put("RadioButtonMenuItem.font",sf);  //$NON-NLS-1$
			UIManager.put("CheckBoxMenuItem.font",sf);  //$NON-NLS-1$
			UIManager.put("PopupMenu.font",sf);  //$NON-NLS-1$
			UIManager.put("OptionPane.font",sf);  //$NON-NLS-1$
			UIManager.put("Panel.font",sf);  //$NON-NLS-1$
			UIManager.put("ProgressBar.font",sf);  //$NON-NLS-1$
			UIManager.put("ScrollPane.font",sf);  //$NON-NLS-1$
			UIManager.put("Viewport",sf);  //$NON-NLS-1$
			UIManager.put("TabbedPane.font",sf);  //$NON-NLS-1$
			UIManager.put("TableHeader.font",sf);  //$NON-NLS-1$
			UIManager.put("TextField.font",sf);  //$NON-NLS-1$
			UIManager.put("PasswordFiled.font",sf);  //$NON-NLS-1$
			UIManager.put("TextArea.font",sf);  //$NON-NLS-1$
			UIManager.put("TextPane.font",sf);  //$NON-NLS-1$
			UIManager.put("EditorPane.font",sf);  //$NON-NLS-1$
			UIManager.put("TitledBorder.font",sf);  //$NON-NLS-1$
			UIManager.put("ToolBar.font",sf);  //$NON-NLS-1$
			UIManager.put("ToolTip.font",sf);  //$NON-NLS-1$
			UIManager.put("Tree.font",sf);  //$NON-NLS-1$
		}
		
		setTitle("Test"); //$NON-NLS-1$
		setIconImage(readImageIcon("Play1Hot_32.png").getImage()); //$NON-NLS-1$
		
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		JComponent jp = buildContent();
		String showScrollbars = System.getProperty("scrollbars", "").toLowerCase(); //$NON-NLS-1$

		/*
		 * handle scrollbars:
		 *
		 * 1) if scrollbars have been forced (-Dscrollbars=true), always display them
		 * 2) if scrollbars have been disabled (-Dscrollbars=false), never display them
		 * 3) otherwise display them as needed
		 *
		 */
		if (showScrollbars == "true") {
			setContentPane(
				new JScrollPane(
					jp,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS
				)
			);
		} else if (showScrollbars == "false") {
			setContentPane(jp);
		} else {
			setContentPane(
				new JScrollPane(
					jp,
					ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
				)
			);
		}

		this.setTitle("Java PS3 Media Server v" + PMS.VERSION); //$NON-NLS-1$
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		Dimension screenSize = getToolkit().getScreenSize();

		if (screenSize.width < MINIMUM_SIZE.width || screenSize.height < MINIMUM_SIZE.height) {
			setMinimumSize(screenSize);
		} else {
			setMinimumSize(MINIMUM_SIZE);
		}

		if (screenSize.width < PREFERRED_SIZE.width || screenSize.height < PREFERRED_SIZE.height) {
			setSize(screenSize);
		} else {
			setSize(PREFERRED_SIZE);
		}

		setResizable(true);
		Dimension paneSize = getSize();
		setLocation(
			((screenSize.width > paneSize.width) ? ((screenSize.width  - paneSize.width)  / 2) : 0),
			((screenSize.height > paneSize.height) ? ((screenSize.height  - paneSize.height)  / 2) : 0)
		);
		if (!PMS.getConfiguration().isMinimized() && System.getProperty(START_SERVICE) == null)
			setVisible(true);
		PMSUtil.addSystemTray(this);
	}
	
	protected static ImageIcon readImageIcon(String filename) {
		URL url = LooksFrame.class.getResource("/resources/images/" + filename); //$NON-NLS-1$
		return new ImageIcon(url);
	}

	public JComponent buildContent() {
		JPanel panel = new JPanel(new BorderLayout());
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		
		toolBar.add(new JPanel());
		AbstractButton save = createToolBarButton(Messages.getString("LooksFrame.9"), "filesave-48.png", Messages.getString("LooksFrame.9")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		save.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				PMS.get().save();
			}
			
		});
		toolBar.add(save);
		toolBar.addSeparator();
		reload = createToolBarButton(Messages.getString("LooksFrame.12"), "reload_page-48.png", Messages.getString("LooksFrame.12")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		reload.setEnabled(false);
		reload.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				try {
					PMS.get().reset();
				} catch (IOException e1) {
				PMS.error(null, e1);
				}
			}
			
		});
		toolBar.add(reload);
		toolBar.addSeparator();
		AbstractButton quit = createToolBarButton(Messages.getString("LooksFrame.5"), "exit-48.png", Messages.getString("LooksFrame.5")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		quit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				quit();
			}
			
		});
		toolBar.add(quit);
		if (System.getProperty(START_SERVICE) != null)
			quit.setEnabled(false);
		toolBar.add(new JPanel());
		panel.add(toolBar, BorderLayout.NORTH);
		panel.add(buildMain(), BorderLayout.CENTER);
		status = new JLabel(" "); //$NON-NLS-1$
		status.setBorder(new CompoundBorder(new EtchedBorder(), new EmptyBorder(0, 5, 0, 5)));
		panel.add(status, BorderLayout.SOUTH);
		return panel;
	}
	
	public JComponent buildMain() {
		 JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
		//tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		 st = new StatusTab();
		 tt = new TracesTab();
		 tr = new TrTab2(configuration);
		 nt = new NetworkTab(configuration);
		 ft = new FoldTab(configuration);
		 
		 tabbedPane.addTab(Messages.getString("LooksFrame.18"),/* readImageIcon("server-16.png"),*/ st.build()); //$NON-NLS-1$
		 tabbedPane.addTab(Messages.getString("LooksFrame.19"),/* readImageIcon("mail_new-16.png"),*/ tt.build()); //$NON-NLS-1$
		 
		 tabbedPane.addTab(Messages.getString("LooksFrame.20"),/* readImageIcon("advanced-16.png"),*/ nt.build()); //$NON-NLS-1$
		 tabbedPane.addTab(Messages.getString("LooksFrame.22"), /*readImageIcon("bookmark-16.png"),*/ ft.build()); //$NON-NLS-1$
		 tabbedPane.addTab(Messages.getString("LooksFrame.21"),/* readImageIcon("player_play-16.png"),*/tr.build()); //$NON-NLS-1$
		 tabbedPane.addTab(Messages.getString("LooksFrame.23"),/*  readImageIcon("mail_new-16.png"), */new AboutTab().build()); //$NON-NLS-1$
		 tabbedPane.addTab(Messages.getString("LooksFrame.24"), /* readImageIcon("mail_new-16.png"), */new FAQTab().build()); //$NON-NLS-1$
		 tabbedPane.addTab(Messages.getString("LooksFrame.25"), /*readImageIcon("documentinfo-16.png"),*/ new LinksTab().build()); //$NON-NLS-1$

			tabbedPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		return tabbedPane;
	}
	
	 protected AbstractButton createToolBarButton(String text, String iconName, String toolTipText) {
			JButton button = new JButton(text, readImageIcon(iconName));
			button.setToolTipText(toolTipText);
			button.setFocusable(false);
			return button;
		}
	
	 
	 public void quit() {
		 WindowsNamedPipe.loop = false;
		 try {
			Thread.sleep(100);
		} catch (InterruptedException e) {}
		 System.exit(0);
	 }

	@Override
	public void append(String msg) {
		tt.getList().append(msg);
	}

	@Override
	public void setReadValue(long v, String msg) {
		st.setReadValue(v, msg);
	}

	@Override
	public void setStatusCode(int code, String msg, String icon) {
		st.getJl().setText(msg);
		try {
			st.getImagePanel().set(ImageIO.read(LooksFrame.class.getResourceAsStream("/resources/images/" + icon))); //$NON-NLS-1$
		} catch (IOException e) {
			PMS.error(null, e);
		}
	}

	@Override
	public void setValue(int v, String msg) {
		st.getJpb().setValue(v);
		st.getJpb().setString(msg);
	}

	@Override
	public void setReloadable(boolean b) {
		reload.setEnabled(b);
	}

	@Override
	public void addEngines() {
		tr.addEngines();
	}
	
	public void update(Observable o, Object arg) {
		if (o == autoUpdater) {
			try {
				AutoUpdateDialog.showIfNecessary(this, autoUpdater);
			} catch (NoClassDefFoundError ncdf) {
				PMS.minimal("Class not found: " + ncdf.getMessage()); //$NON-NLS-1$
			}
		}
	}
	
	public void setStatusLine(String line) {
		if (line == null)
			line = " "; //$NON-NLS-1$
		status.setText(line);
	}

	@Override
	public void addRendererIcon(int code, String msg, String icon) {
		st.addRendererIcon(code, msg, icon);
	}
	
	@Override
	public void serverReady() {
		nt.addPlugins();
	}
}
