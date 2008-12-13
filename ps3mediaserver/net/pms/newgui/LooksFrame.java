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

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

import net.pms.PMS;
import net.pms.gui.IFrame;
import net.pms.io.WindowsNamedPipe;

import com.jgoodies.looks.BorderStyle;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.windows.WindowsLookAndFeel;

public class LooksFrame extends JFrame implements IFrame {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8723727186288427690L;
	public TracesTab getTt() {
		return tt;
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

	protected static final Dimension PREFERRED_SIZE = new Dimension(880, 640);


	/**
     * Constructs a <code>DemoFrame</code>, configures the UI,
     * and builds the content.
     */
    public LooksFrame() {
       
    	Options.setDefaultIconSize(new Dimension(18, 18));

        Options.setUseNarrowButtons(true);

        // Global options
        Options.setTabIconsEnabled(true);
        UIManager.put(Options.POPUP_DROP_SHADOW_ENABLED_KEY, null);

        // Swing Settings
        LookAndFeel selectedLaf = null;
        if (PMS.get().isWindows()) {
        	selectedLaf = new WindowsLookAndFeel();
        }
        else
        	selectedLaf = new PlasticLookAndFeel();
        
        if (selectedLaf instanceof PlasticLookAndFeel) {
            PlasticLookAndFeel.setPlasticTheme(PlasticLookAndFeel.createMyDefaultTheme());
            PlasticLookAndFeel.setTabStyle(PlasticLookAndFeel.TAB_STYLE_DEFAULT_VALUE);
            PlasticLookAndFeel.setHighContrastFocusColorsEnabled(false);
        } else if (selectedLaf.getClass() == MetalLookAndFeel.class) {
            MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
        }

        // Work around caching in MetalRadioButtonUI
        JRadioButton radio = new JRadioButton();
        radio.getUI().uninstallUI(radio);
        JCheckBox checkBox = new JCheckBox();
        checkBox.getUI().uninstallUI(checkBox);

        try {
            UIManager.setLookAndFeel(selectedLaf);
        } catch (Exception e) {
            System.out.println("Can't change L&F: " + e);
        }
        
        setTitle("Test");
        setIconImage(readImageIcon("Play1Hot_32.png").getImage());
        
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        
        setContentPane(buildContent());
        this.setTitle("Java PS3 Media Server v" + PMS.VERSION);
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		 setSize(PREFERRED_SIZE);
	        setResizable(false);
	        Dimension paneSize = getSize();
	        Dimension screenSize = getToolkit().getScreenSize();
	        setLocation(
	            (screenSize.width  - paneSize.width)  / 2,
	            (screenSize.height - paneSize.height) / 2);
	        if (!PMS.get().isMinimized())
	        setVisible(true);
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();

			Image image = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/resources/images/Play1Hot_256.png"));

			PopupMenu popup = new PopupMenu();
			MenuItem defaultItem = new MenuItem("Quit");
			MenuItem traceItem = new MenuItem("Main Panel");

			defaultItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quit();
			}
			});

			traceItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(true);
			}
			});

			popup.add(traceItem);
			popup.add(defaultItem);

			final TrayIcon trayIcon = new TrayIcon(image, "Java PS3 Media Server v" + PMS.VERSION, popup);

			trayIcon.setImageAutoSize(true);
			trayIcon.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(true);
				setFocusable(true);
			}

			});
			try {
			tray.add(trayIcon);
			} catch (AWTException e) {
			e.printStackTrace();
			}
			}
    }
	
	public static void main(String[] args) {
        LooksFrame instance = new LooksFrame();
        instance.setSize(PREFERRED_SIZE);
        instance.setResizable(false);
        Dimension paneSize = instance.getSize();
        Dimension screenSize = instance.getToolkit().getScreenSize();
        instance.setLocation(
            (screenSize.width  - paneSize.width)  / 2,
            (screenSize.height - paneSize.height) / 2);
        instance.setVisible(true);
	}
	
	
	protected static ImageIcon readImageIcon(String filename) {
        URL url = LooksFrame.class.getResource("/resources/images/" + filename);
        return new ImageIcon(url);
    }

	public JComponent buildContent() {
		JPanel panel = new JPanel(new BorderLayout());
		JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        toolBar.putClientProperty(WindowsLookAndFeel.BORDER_STYLE_KEY, BorderStyle.EMPTY);
        
        toolBar.add(new JPanel());
        AbstractButton save = createToolBarButton("Save", "filesave-48.png", "Save");
        save.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				PMS.get().save();
			}
        	
        });
        toolBar.add(save);
        toolBar.addSeparator();
        reload = createToolBarButton("Restart HTTP", "reload_page-48.png", "Restart HTTP Server");
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
        AbstractButton quit = createToolBarButton("Quit", "exit-48.png", "Quit");
        quit.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				quit();
			}
        	
        });
        toolBar.add(quit);
        toolBar.add(new JPanel());
        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(buildMain(), BorderLayout.CENTER);
        
        return panel;
	}
	
	public JComponent buildMain() {
		 JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
	        //tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

		 st = new StatusTab();
		 tt = new TracesTab();
		 tr = new TrTab2();
		 nt = new NetworkTab();
		 
		 tabbedPane.addTab("Status", readImageIcon("server-16.png"), st.build());
		 tabbedPane.addTab("Traces", readImageIcon("mail_new-16.png"), tt.build());
		 
		 tabbedPane.addTab("General Configuration", readImageIcon("advanced-16.png"), nt.build());
		 tabbedPane.addTab("Transcoding Settings", readImageIcon("player_play-16.png"),tr.build());
		 tabbedPane.addTab("Folders Sharing", readImageIcon("bookmark-16.png"), new FoldTab().build());
		 tabbedPane.addTab("Readme",  readImageIcon("mail_new-16.png"), new AboutTab().build());
		 tabbedPane.addTab("FAQ / Help",  readImageIcon("mail_new-16.png"), new FAQTab().build());
		 tabbedPane.addTab("About", readImageIcon("documentinfo-16.png"), new LinksTab().build());

	        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));
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
			st.getImagePanel().set(ImageIO.read(LooksFrame.class.getResourceAsStream("/resources/images/" + icon)));
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
}