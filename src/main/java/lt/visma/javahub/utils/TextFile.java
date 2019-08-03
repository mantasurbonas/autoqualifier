package lt.visma.javahub.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;

/****
 * a quick and very inefficient implementation for working with text-line files.
 * contains all content in memory.
 * 
 * @author mantas.urbonas
 *
 */
public class TextFile implements AutoCloseable{

	private Charset charset = Charset.forName("UTF-8");
	private boolean open = false;

	private ArrayList<String> lines = null;
	
	private File file;

	public TextFile(String path) {
		this.file = new File(path);
	}
	
	public TextFile(File path) throws IOException {
		this.file = path;
	}
	
	public TextFile(Path path) throws IOException {
		this.file = path.toFile();	
	}
	
	public TextFile setCharset(String charsetName) {
		this.charset = Charset.forName(charsetName);
		return this;
	}
	
	public TextFile setCharset(Charset charset) {
		this.charset = charset;
		return this;
	}
		
	public boolean isOpen() {
		return open ;
	}
	
	public String getLine(int lineNumber) {
		if (!open)
			doOpen();
		
		return lines.get(lineNumber);
	}
	
	public TextFile insertLine(int lineNumber, String text) {
		if (!open)
			doOpen();
		
		lines.add(lineNumber, text);
		
		return this;
	}
	
	public TextFile removeLine(int lineNumber) {
		if (!open)
			doOpen();
		
		lines.remove(lineNumber);
		
		return this;
	}
	
	public TextFile replaceLine(int lineNumber, String text) {
		if (!open)
			doOpen();
		
		lines.set(lineNumber, text);
		
		return this;		
	}
	
	public TextFile appendText(int lineNumber, String text) {
		String originalLine = getLine(lineNumber);
		
		return replaceLine(lineNumber, originalLine + text);
	}
	
	public TextFile prependText(int lineNumber, String text) {
		String originalLine = getLine(lineNumber);
		
		return replaceLine(lineNumber, text + originalLine);
	}

	public TextFile save() {
		if (!open)
			return this;
		
		try(BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsolutePath(), false))){
			for (String line: lines)
				writer.write(line + "\r\n");
			
			writer.flush();
		}catch(Exception e) {
			throw new RuntimeException(e);
		}
		
		return this;
	}
	
	@Override
	public void close() {
		if (!open)
			return;
		
		open = false;
		lines = null;
	}
	
	protected void doOpen()  {
		if (open)
			return;
		
		lines = new ArrayList<>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file, charset))) {
            for (;;) {
                String line = reader.readLine();
                if (line == null)
                    break;
                lines.add(line);
            }
            
            open = true;
            
        }catch(Exception e) {
        	throw new RuntimeException(e);
        }
	}
	
}
