package utils;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class MyFile implements Serializable {

	private static final long serialVersionUID = -8076326593527941165L;
	public boolean isFile = false;
	public boolean isDir = true;
	public long lastModif = 0L;
	public long length = 0L;
	public String name;
	public boolean r;
	public boolean w;
	public boolean hidden;
	public String path;

	public ArrayList<MyFile> list;

	public MyFile(File f) {
		name = f.getName();
		length = f.length();
		lastModif = f.lastModified();
		isFile = f.isFile();
		isDir = f.isDirectory();
		r = f.canRead();
		w = f.canWrite();
		hidden = f.isHidden();
		path = f.getPath();
		list = new ArrayList<MyFile>();
	}

	public ArrayList<MyFile> getList() {
		return list;
	}

	public void addChild(MyFile c) {
		list.add(c);
	}

	public boolean isFile() {
		return isFile;
	}

	public void setFile(boolean isFile) {
		this.isFile = isFile;
	}

	public boolean isDir() {
		return isDir;
	}

	public void setDir(boolean isDir) {
		this.isDir = isDir;
	}

	public long getLastModif() {
		return lastModif;
	}

	public void setLastModif(long lastModif) {
		this.lastModif = lastModif;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isR() {
		return r;
	}

	public void setR(boolean r) {
		this.r = r;
	}

	public boolean isW() {
		return w;
	}

	public void setW(boolean w) {
		this.w = w;
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public byte[] build() {
		return null;
	}

	public void parse(byte[] packet) {
	}
}