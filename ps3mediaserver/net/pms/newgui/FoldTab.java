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

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.util.KeyedComboBoxModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;

public class FoldTab {
	
	public static final String ALL_DRIVES = Messages.getString("FoldTab.0"); //$NON-NLS-1$
	private JList FList;
	private  DefaultListModel df;
	private JCheckBox  hidevideosettings;
	private JCheckBox  hidetranscode;
	private JCheckBox  hidemedialibraryfolder;
	private JCheckBox  hideextensions;
	private JCheckBox  hideemptyfolders;
	private JCheckBox  hideengines;
	private JButton but5;
	private JTextField seekpos;
	private JCheckBox  tncheckBox;
	private JCheckBox  mplayer_thumb;
	private JCheckBox  dvdiso_thumb;
	private JCheckBox  cacheenable;
	private JCheckBox  archive;
	private JComboBox sortmethod;
	private JComboBox audiothumbnail;
	private JTextField defaultThumbFolder;
 	private JCheckBox  iphoto;
 	private JCheckBox  itunes;

	public DefaultListModel getDf() {
		return df;
	}
	
	private final PmsConfiguration configuration;
	
	FoldTab(PmsConfiguration configuration) {
		this.configuration = configuration;
	}
	
	private void updateModel() {
		if (df.size() == 1 && df.getElementAt(0).equals(ALL_DRIVES)) {
			PMS.getConfiguration().setFolders(""); //$NON-NLS-1$
		} else {
			StringBuffer sb = new StringBuffer();
			for(int i=0;i<df.size();i++) {
				if (i> 0)
					sb.append(","); //$NON-NLS-1$
				sb.append(df.getElementAt(i));
			}
			PMS.getConfiguration().setFolders(sb.toString());
		}
		PMS.get().getFrame().setReloadable(true);
	}

	public JComponent build() {
		FormLayout layout = new FormLayout(
                "left:pref, 50dlu, pref, 150dlu, pref, 25dlu, pref, 25dlu, pref, default:grow", //$NON-NLS-1$
                "p, 3dlu,  p, 3dlu, p, 3dlu,  p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu,  p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, fill:default:grow"); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.DLU4_BORDER);
        builder.setOpaque(true);

        CellConstraints cc = new CellConstraints();
        
       df = new DefaultListModel();
       if (PMS.getConfiguration().getFolders() != null && PMS.getConfiguration().getFolders().length() > 0) {
    	 try {
			File f [] =  PMS.get().loadFoldersConf(PMS.getConfiguration().getFolders());
			for(File file:f) {
				df.addElement(file.getAbsolutePath());
			}
			if (f == null || f.length == 0) {
				df.addElement(ALL_DRIVES);
			}
		} catch (IOException e1) {
			PMS.error(null, e1);
		}
       } else
		df.addElement(ALL_DRIVES);
		FList = new JList();
		FList.setModel(df);
        JScrollPane pane = new JScrollPane(FList);
        
        JComponent cmp = builder.addSeparator(Messages.getString("FoldTab.13"),  cc.xyw(1, 1, 10)); //$NON-NLS-1$
        cmp = (JComponent) cmp.getComponent(0);
        cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
        
        tncheckBox = new JCheckBox(Messages.getString("NetworkTab.2")); //$NON-NLS-1$
        tncheckBox.setContentAreaFilled(false);
        tncheckBox.addItemListener(new ItemListener() {

  			public void itemStateChanged(ItemEvent e) {
  				PMS.getConfiguration().setThumbnailsEnabled((e.getStateChange() == ItemEvent.SELECTED));
  			}
        	
        });
        if (PMS.getConfiguration().getThumbnailsEnabled())
        	tncheckBox.setSelected(true);
        builder.add(tncheckBox,          cc.xyw(1,  3, 3));
        
        
        seekpos = new JTextField("" + configuration.getThumbnailSeekPos()); //$NON-NLS-1$
        seekpos.addKeyListener(new KeyListener() {

    		@Override
    		public void keyPressed(KeyEvent e) {}
    		@Override
    		public void keyTyped(KeyEvent e) {}
    		@Override
    		public void keyReleased(KeyEvent e) {
    			try {
    				int ab = Integer.parseInt(seekpos.getText());
    				configuration.setThumbnailSeekPos(ab);
    			} catch (NumberFormatException nfe) {
    			}
    			
    		}
        	   
           });
         
        
        
        builder.addLabel(Messages.getString("NetworkTab.16"),  cc.xyw(4,  3, 3)); //$NON-NLS-1$
        builder.add(seekpos,          cc.xyw(6,  3, 2));
        
        
        mplayer_thumb = new JCheckBox(Messages.getString("FoldTab.14")); //$NON-NLS-1$
        mplayer_thumb.setContentAreaFilled(false);
        mplayer_thumb.addItemListener(new ItemListener() {

  			public void itemStateChanged(ItemEvent e) {
  				PMS.getConfiguration().setUseMplayerForVideoThumbs((e.getStateChange() == ItemEvent.SELECTED));
  			}
        	
        });
        if (PMS.getConfiguration().isUseMplayerForVideoThumbs())
        	mplayer_thumb.setSelected(true);
        builder.add(mplayer_thumb,          cc.xyw(1,  5, 3));
        
        dvdiso_thumb = new JCheckBox(Messages.getString("FoldTab.19"));
        dvdiso_thumb.setContentAreaFilled(false);
        dvdiso_thumb.addItemListener(new ItemListener() {

  			public void itemStateChanged(ItemEvent e) {
  				PMS.getConfiguration().setDvdIsoThumbnails((e.getStateChange() == ItemEvent.SELECTED));
  			}
        	
        });
        if (PMS.getConfiguration().isDvdIsoThumbnails())
        	dvdiso_thumb.setSelected(true);
        builder.add(dvdiso_thumb,          cc.xyw(3,  5, 3));
        
        final KeyedComboBoxModel thumbKCBM = new KeyedComboBoxModel(new Object[] { "0", "1", "2" }, new Object[] { Messages.getString("FoldTab.15"), Messages.getString("FoldTab.23"), Messages.getString("FoldTab.24") }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        audiothumbnail = new JComboBox(thumbKCBM);
        audiothumbnail.setEditable(false);
       
        thumbKCBM.setSelectedKey("" + configuration.getAudioThumbnailMethod()); //$NON-NLS-1$
      
      audiothumbnail.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					
 					try {
 						configuration.setAudioThumbnailMethod(Integer.parseInt((String) thumbKCBM.getSelectedKey()));
 						PMS.get().getFrame().setReloadable(true);
 					} catch (NumberFormatException nfe) {}
 					
 				}
 			}
      	
      });
      builder.addLabel(Messages.getString("FoldTab.26"), cc.xyw(1,7,3)); //$NON-NLS-1$
        builder.add(audiothumbnail, cc.xyw(4, 7,4));
        
        builder.addLabel(Messages.getString("FoldTab.27"), cc.xyw(1, 9, 3)); //$NON-NLS-1$
        defaultThumbFolder = new JTextField(configuration.getAlternateThumbFolder());
        defaultThumbFolder.addKeyListener(new KeyListener() {

    		@Override
    		public void keyPressed(KeyEvent e) {}
    		@Override
    		public void keyTyped(KeyEvent e) {}
    		@Override
    		public void keyReleased(KeyEvent e) {
    			configuration.setAlternateThumbFolder(defaultThumbFolder.getText());
    		}
        	   
           });
        builder.add(defaultThumbFolder, cc.xyw(4, 9, 3));
        
        JButton select = new JButton("..."); //$NON-NLS-1$
        select.addActionListener(new ActionListener() {

 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser chooser = null;
				try {
					 chooser = new JFileChooser();
				} catch (Exception ee) {
					 chooser = new JFileChooser(new RestrictedFileSystemView());
				}
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("FoldTab.28")); //$NON-NLS-1$
 			    if(returnVal == JFileChooser.APPROVE_OPTION) {
 			    	defaultThumbFolder.setText(chooser.getSelectedFile().getAbsolutePath());
 			    	PMS.get().getFrame().setReloadable(true);
 			    	configuration.setAlternateThumbFolder(chooser.getSelectedFile().getAbsolutePath());
 			    }
 			}
  		  
  	  });
  	  builder.add(select,          cc.xyw(7,  9, 1));
        
        cmp = builder.addSeparator(Messages.getString("NetworkTab.15"),  cc.xyw(1, 11, 10)); //$NON-NLS-1$
        cmp = (JComponent) cmp.getComponent(0);
        cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
        
        hidevideosettings = new JCheckBox(Messages.getString("FoldTab.6")); //$NON-NLS-1$
        hidevideosettings.setContentAreaFilled(false);
        if (PMS.getConfiguration().getHideVideoSettings())
        	hidevideosettings.setSelected(true);
        hidevideosettings.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.getConfiguration().setHideVideoSettings((e.getStateChange() == ItemEvent.SELECTED));
 				PMS.get().getFrame().setReloadable(true);
 			}
        	
        });
        
     	hidetranscode = new JCheckBox(Messages.getString("FoldTab.33")); //$NON-NLS-1$
        hidetranscode.setContentAreaFilled(false);
        if (PMS.getConfiguration().getHideTranscodeEnabled())
        	hidetranscode.setSelected(true);
	hidetranscode.addItemListener(new ItemListener() {
  		public void itemStateChanged(ItemEvent e) {
  			PMS.getConfiguration().setHideTranscodeEnabled((e.getStateChange() == ItemEvent.SELECTED));
  			PMS.get().getFrame().setReloadable(true);
  		}	
        });

        hidemedialibraryfolder = new JCheckBox(Messages.getString("FoldTab.32")); //$NON-NLS-1$
        hidemedialibraryfolder.setContentAreaFilled(false);
        if (PMS.getConfiguration().isHideMediaLibraryFolder())
        	hidemedialibraryfolder.setSelected(true);
        hidemedialibraryfolder.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.getConfiguration().setHideMediaLibraryFolder((e.getStateChange() == ItemEvent.SELECTED));
 				PMS.get().getFrame().setReloadable(true);
 			}
        	
        });
    	
        

        
        archive = new JCheckBox(Messages.getString("NetworkTab.1")); //$NON-NLS-1$
        archive.setContentAreaFilled(false);
        archive.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.getConfiguration().setArchiveBrowsing(e.getStateChange() == ItemEvent.SELECTED);
 				if (PMS.get().getFrame() != null)
 					PMS.get().getFrame().setReloadable(true);
 			}
        	
        });
        if (PMS.getConfiguration().isArchiveBrowsing())
     	   archive.setSelected(true);
        
        
        
     
        builder.add(archive,          cc.xyw(1,  13, 3));
        
        /*disablefakesize = new JCheckBox(Messages.getString("FoldTab.11"));  //$NON-NLS-1$
        disablefakesize.setContentAreaFilled(false);
        if (PMS.getConfiguration().isDisableFakeSize())
        	disablefakesize.setSelected(true);
        disablefakesize.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.getConfiguration().setDisableFakeSize((e.getStateChange() == ItemEvent.SELECTED));
 				PMS.get().getFrame().setReloadable(true);
 			}
        	
        });
        builder.add(disablefakesize,          cc.xyw(1,  7, 7));
        */
        final JButton cachereset = new JButton(Messages.getString("NetworkTab.18")); //$NON-NLS-1$
  	  
        cacheenable = new JCheckBox(Messages.getString("NetworkTab.17")); //$NON-NLS-1$
        cacheenable.setContentAreaFilled(false);
        cacheenable.setSelected(PMS.getConfiguration().getUseCache());
        cacheenable.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.getConfiguration().setUseCache((e.getStateChange() == ItemEvent.SELECTED));
 				cachereset.setEnabled(PMS.getConfiguration().getUseCache());
 				PMS.get().getFrame().setReloadable(true);
 				if ((LooksFrame) PMS.get().getFrame() != null)
 					((LooksFrame) PMS.get().getFrame()).getFt().setScanLibraryEnabled(PMS.getConfiguration().getUseCache());
 			}
       	
       });
      
       
        //cacheenable.setEnabled(false);
        
     	  builder.add(cacheenable,          cc.xy(1,  19));
     	  
     	  
     	  cachereset.addActionListener(new ActionListener() {

 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int option = JOptionPane.showConfirmDialog(
    	                    (Component) PMS.get().getFrame(),
    	                    Messages.getString("NetworkTab.13") +  //$NON-NLS-1$
    	                    Messages.getString("NetworkTab.19"), //$NON-NLS-1$
    	                    "Question", //$NON-NLS-1$
    	                    JOptionPane.YES_NO_OPTION
    	                    );
    				if (option == JOptionPane.YES_OPTION) {
    					PMS.get().getDatabase().init(true);
    				}
 				
 			}
     		  
     	  });
     	  builder.add(cachereset,          cc.xyw(4,  19, 4));
     	  
     	  
     	  cachereset.setEnabled(PMS.getConfiguration().getUseCache());
        
        builder.add(hidevideosettings,          cc.xyw(4,  13, 3));

        builder.add(hidetranscode,          cc.xyw(8,  13, 3));
        
        builder.add(hidemedialibraryfolder,          cc.xyw(8,  19, 3));
        
        hideextensions = new JCheckBox(Messages.getString("FoldTab.5")); //$NON-NLS-1$
        hideextensions.setContentAreaFilled(false);
        if (PMS.getConfiguration().isHideExtensions())
        	hideextensions.setSelected(true);
        hideextensions.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.getConfiguration().setHideExtensions((e.getStateChange() == ItemEvent.SELECTED));
 				PMS.get().getFrame().setReloadable(true);
 			}
        	
        });
        builder.add(hideextensions,          cc.xyw(1,  15, 3));
        
        hideengines = new JCheckBox(Messages.getString("FoldTab.8")); //$NON-NLS-1$
        hideengines.setContentAreaFilled(false);
        if (PMS.getConfiguration().isHideEngineNames())
        	hideengines.setSelected(true);
        hideengines.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.getConfiguration().setHideEngineNames((e.getStateChange() == ItemEvent.SELECTED));
 				PMS.get().getFrame().setReloadable(true);
 			}
        	
        });
        builder.add(hideengines,          cc.xyw(4,  15, 3));
        
        hideemptyfolders = new JCheckBox(Messages.getString("FoldTab.31")); //$NON-NLS-1$
        hideemptyfolders.setContentAreaFilled(false);
        if (PMS.getConfiguration().isHideEmptyFolders())
        	hideemptyfolders.setSelected(true);
        hideemptyfolders.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.getConfiguration().setHideEmptyFolders((e.getStateChange() == ItemEvent.SELECTED));
 				PMS.get().getFrame().setReloadable(true);
 			}
        	
        });
        builder.add(hideemptyfolders,          cc.xyw(8,  15, 3));
        
 	itunes = new JCheckBox(Messages.getString("FoldTab.30")); //$NON-NLS-1$
        itunes.setContentAreaFilled(false);
        if (PMS.getConfiguration().getItunesEnabled())
        	itunes.setSelected(true);
        if (!(Platform.isMac() || Platform.isWindows()))
	        itunes.setEnabled(false);
        itunes.addItemListener(new ItemListener() {
 
  		public void itemStateChanged(ItemEvent e) {
  			PMS.getConfiguration().setItunesEnabled((e.getStateChange() == ItemEvent.SELECTED));
  			PMS.get().getFrame().setReloadable(true);
  		}
         	
        });
        builder.add(itunes,          cc.xyw(1,  17, 3));

 	iphoto = new JCheckBox(Messages.getString("FoldTab.29")); //$NON-NLS-1$
        iphoto.setContentAreaFilled(false);
        if (PMS.getConfiguration().getIphotoEnabled())
        	iphoto.setSelected(true);
        if (!Platform.isMac())
	        iphoto.setEnabled(false);
	iphoto.addItemListener(new ItemListener() {
  		public void itemStateChanged(ItemEvent e) {
  			PMS.getConfiguration().setIphotoEnabled((e.getStateChange() == ItemEvent.SELECTED));
  			PMS.get().getFrame().setReloadable(true);
  		}	
        });
        builder.add(iphoto,          cc.xyw(4,  17, 3));
       
        final KeyedComboBoxModel kcbm = new KeyedComboBoxModel(new Object[] { "0", "1" }, new Object[] { Messages.getString("FoldTab.15"), Messages.getString("FoldTab.16") }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        sortmethod = new JComboBox(kcbm);
        sortmethod.setEditable(false);

      kcbm.setSelectedKey("" + configuration.getSortMethod()); //$NON-NLS-1$
      
      sortmethod.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				if (e.getStateChange() == ItemEvent.SELECTED) {
 					
 					try {
 						configuration.setSortMethod(Integer.parseInt((String) kcbm.getSelectedKey()));
 						PMS.get().getFrame().setReloadable(true);
 					} catch (NumberFormatException nfe) {}
 					
 				}
 			}
      	
      });
      
      builder.addLabel(Messages.getString("FoldTab.18"), cc.xyw(1,21,3)); //$NON-NLS-1$
        builder.add(sortmethod, cc.xyw(4, 21,4));
        
       
        
        
        FormLayout layoutFolders = new FormLayout(
                "left:pref, left:pref, pref, pref, pref, 0:grow", //$NON-NLS-1$
                "p, 3dlu, p, 3dlu, fill:default:grow"); //$NON-NLS-1$
         PanelBuilder builderFolder = new PanelBuilder(layoutFolders);
          builderFolder.setOpaque(true);

        
       cmp =  builderFolder.addSeparator(Messages.getString("FoldTab.7"),  cc.xyw(1, 1,6)); //$NON-NLS-1$
        cmp = (JComponent) cmp.getComponent(0);
        cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
        
       JButton but = new JButton(LooksFrame.readImageIcon("folder_new-32.png")); //$NON-NLS-1$
       //but.setBorder(BorderFactory.createEmptyBorder());
       but.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JFileChooser chooser = null;
				try {
					 chooser = new JFileChooser();
				} catch (Exception ee) {
					 chooser = new JFileChooser(new RestrictedFileSystemView());
				}
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				//int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("FoldTab.9")); //$NON-NLS-1$
				int returnVal = chooser.showOpenDialog((Component) e.getSource());
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	((DefaultListModel) FList.getModel()).add(FList.getModel().getSize(),chooser.getSelectedFile().getAbsolutePath());
			    	if (FList.getModel().getElementAt(0).equals(ALL_DRIVES))
			    		((DefaultListModel) FList.getModel()).remove(0);
			    	updateModel();
			    }
			}
		});
       builderFolder.add(but,          cc.xy(1,  3));
       JButton but2 = new JButton(LooksFrame.readImageIcon("button_cancel-32.png")); //$NON-NLS-1$
       //but2.setBorder(BorderFactory.createEtchedBorder());
       but2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (FList.getSelectedIndex() > -1) {
					((DefaultListModel) FList.getModel()).remove(FList.getSelectedIndex());
					if (FList.getModel().getSize() == 0)
			    		((DefaultListModel) FList.getModel()).add(0, ALL_DRIVES);
					updateModel();
				}
			}
		});
       builderFolder.add(but2,          cc.xy(2,  3));
       
       JButton but3 = new JButton(LooksFrame.readImageIcon("kdevelop_down-32.png")); //$NON-NLS-1$
       but3.setToolTipText(Messages.getString("FoldTab.12")); //$NON-NLS-1$
      // but3.setBorder(BorderFactory.createEmptyBorder());
       but3.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			DefaultListModel model = ((DefaultListModel) FList.getModel());
			for(int i=0;i<model.size()-1;i++) {
				if (FList.isSelectedIndex(i)) {
					String value = model.get(i).toString();
					model.set(i, model.get(i+1));
					model.set(i+1, value);
					FList.setSelectedIndex(i+1);
					updateModel();
					break;
				}
			}
		}   	   
       });
       
       
       
       builderFolder.add(but3,          cc.xy(3,  3));
       JButton but4 = new JButton(LooksFrame.readImageIcon("up-32.png")); //$NON-NLS-1$
       but4.setToolTipText(Messages.getString("FoldTab.12")); //$NON-NLS-1$
     //  but4.setBorder(BorderFactory.createEmptyBorder());
       but4.addActionListener(new ActionListener() {
   		public void actionPerformed(ActionEvent e) {
   			DefaultListModel model = ((DefaultListModel) FList.getModel());
   			for(int i=1;i<model.size();i++) {
   				if (FList.isSelectedIndex(i)) {
   					String value = model.get(i).toString();
   					
   					model.set(i, model.get(i-1));
   					model.set(i-1, value);
   					FList.setSelectedIndex(i-1);
   					updateModel();
   					break;

   				}
   			}
   		}   	   
          });
       builderFolder.add(but4,          cc.xy(4,  3));
       
       
      but5 = new JButton(LooksFrame.readImageIcon("search-32.png")); //$NON-NLS-1$
       but5.setToolTipText(Messages.getString("FoldTab.2")); //$NON-NLS-1$
       //but5.setBorder(BorderFactory.createEmptyBorder());
       but5.addActionListener(new ActionListener() {
   		public void actionPerformed(ActionEvent e) {
   			if (PMS.getConfiguration().getUseCache()) {
   				if (!PMS.get().getDatabase().isScanLibraryRunning()) {
	   				int option = JOptionPane.showConfirmDialog(
	   	                    (Component) PMS.get().getFrame(),
	   	                    Messages.getString("FoldTab.3") + //$NON-NLS-1$
	   	                    Messages.getString("FoldTab.4"), //$NON-NLS-1$
	   	                    "Question", //$NON-NLS-1$
	   	                    JOptionPane.YES_NO_OPTION
	   	                    );
	   				if (option == JOptionPane.YES_OPTION) {
	   					PMS.get().getDatabase().scanLibrary();
	   					but5.setIcon(LooksFrame.readImageIcon("viewmagfit-32.png")); //$NON-NLS-1$
	   				}
   				} else {
   					int option = JOptionPane.showConfirmDialog(
	   	                    (Component) PMS.get().getFrame(),
	   	                    Messages.getString("FoldTab.10"), //$NON-NLS-1$
	   	                    "Question", //$NON-NLS-1$
	   	                    JOptionPane.YES_NO_OPTION
	   	                    );
	   				if (option == JOptionPane.YES_OPTION) {
	   					PMS.get().getDatabase().stopScanLibrary();
	   					PMS.get().getFrame().setStatusLine(null);
	   					but5.setIcon(LooksFrame.readImageIcon("search-32.png")); //$NON-NLS-1$
	   				}
   				}
   			}
   		}   	   
          });
       builderFolder.add(but5,          cc.xy(5,  3));
       but5.setEnabled(PMS.getConfiguration().getUseCache());
       
       builderFolder.add(pane,          cc.xyw(1,  5,6));
       
       builder.add(builderFolder.getPanel(), cc.xyw(1, 25, 10));
       
        return builder.getPanel();
	}
	
	public void setScanLibraryEnabled(boolean enabled) {
		but5.setEnabled(enabled);
		but5.setIcon(LooksFrame.readImageIcon("search-32.png")); //$NON-NLS-1$
	}
	
}
