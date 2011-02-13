package org.typeexit.kettle.plugin.steps.ruby;

import java.io.File;
import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TreeItem;

public class RubyStepSamplesHelper {
	
	Image folderImage;
	Image scriptImage;
	Image transformationImage;
	Image webDocumentImage;
	Image sampleImage;
	
	static public enum SampleType{
		DIR,
		SCRIPT,
		TRANS,
		WEB,
		OTHER
	};

	private String getSampleFileName(File f) {
		
		String s = f.getName();
		if (s.matches("^[0-9]+ - .*$")){
			s = s.replaceFirst("^[0-9]+ - ", "");
		}
		
		if (f.isFile()) {
			return s.substring(0, s.lastIndexOf("."));
		}
		
		return s;
	}

	
	private String getSampleFileLabel(File f) {
		
		switch(getSampleType(f)){
		case DIR:
			return "";
		case SCRIPT:
			return "script";
		case WEB:
			return "web document";
		case TRANS:
			return "transformation";
		default:
			return "other";
		}
		
	}

	private SampleType getSampleType(File f){
		
		if (f.isDirectory()) {
			return SampleType.DIR;
		}
		
		String s = f.getName();
		String ext = s.substring(s.lastIndexOf("."));
		
		if (ext.equalsIgnoreCase(".rb")){
			return SampleType.SCRIPT;
		}
		else if (ext.equalsIgnoreCase(".ktr")){
			return SampleType.TRANS; 
		}
		else if (ext.equalsIgnoreCase(".html")){
			return SampleType.WEB;
		}
		
		return SampleType.OTHER;		
		
	}
	
	private Image getSampleFileImage(File f) {
		
		switch(getSampleType(f)){
		case DIR:
			return folderImage;
		case SCRIPT:
			return scriptImage;
		case WEB:
			return webDocumentImage;
		case TRANS:
			return transformationImage;
		default:
			return sampleImage;
		}
		
	}

	public void fillSamplesDir(TreeItem parent, File dir) {

		File[] files = dir.listFiles();
		if (files != null) {

			Arrays.sort(files);

			for (int i = 0; i < files.length; i++) {
				TreeItem item = new TreeItem(parent, SWT.NONE);
				item.setText(new String[] { getSampleFileName(files[i]), getSampleFileLabel(files[i]) });
				item.setImage(getSampleFileImage(files[i]));
				item.setData("file", files[i]);
				item.setData("type", getSampleType(files[i]));
				if (files[i].isDirectory()) {
					fillSamplesDir(item, files[i]);
				}
			}

		}

	}


	public void setFolderImage(Image folderImage) {
		this.folderImage = folderImage;
	}


	public void setScriptImage(Image scriptImage) {
		this.scriptImage = scriptImage;
	}


	public void setTransformationImage(Image transformationImage) {
		this.transformationImage = transformationImage;
	}


	public void setWebDocumentImage(Image webDocumentImage) {
		this.webDocumentImage = webDocumentImage;
	}


	public void setSampleImage(Image sampleImage) {
		this.sampleImage = sampleImage;
	}
	
	
}
