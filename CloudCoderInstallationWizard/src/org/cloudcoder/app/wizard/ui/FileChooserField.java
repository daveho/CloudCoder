package org.cloudcoder.app.wizard.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.cloudcoder.app.wizard.model.IValue;
import org.cloudcoder.app.wizard.model.FilenameValue;

public class FileChooserField extends LabeledField<FilenameValue> implements IPageField, UIConstants {
	private static final int BUTTON_WIDTH = 100;
	
	private static final long serialVersionUID = 1L;
	
	private JTextField textField;

	public FileChooserField() {
		textField = new JTextField();
		textField.setPreferredSize(new Dimension(FIELD_COMPONENT_WIDTH - (BUTTON_WIDTH-10), FIELD_COMPONENT_HEIGHT));
		add(textField);
		
		JButton browseButton = new JButton("Browse...");
		add(browseButton);
		browseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				browse();
			}
		});
		
		markValid();
	}
	
	@Override
	public void setValue(FilenameValue value) {
		super.setValue(value);
		textField.setText(value.getString());
	}
	
	@Override
	public void markValid() {
		textField.setBackground(Color.WHITE);
	}

	@Override
	public void markInvalid() {
		textField.setBackground(INVALID_FIELD_BG);
	}

	@Override
	public IValue getCurrentValue() {
		FilenameValue current = getValue().clone();
		current.setString(textField.getText());
		return current;
	}

	private void browse() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "PEM (keypair) files";
			}
			
			@Override
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getAbsolutePath().toLowerCase().endsWith(".pem");
			}
		});
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			textField.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}
}
