import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class MyFuzz3 {
	public static void main(String args[]) throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager fm = compiler.getStandardFileManager(null,
				null, null);

		JavaFileManager fileManager = new ForwardingJavaFileManager(fm) {

			public JavaFileObject getJavaFileForOutput(
					JavaFileManager.Location location, String className,
					JavaFileObject.Kind kind, FileObject sibling)
					throws IOException {
				return new MemoryJavaFileObject("file:///MyFizz4.class",
						kind);
			}
		};

		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

		StringWriter writer = new StringWriter();
		PrintWriter out = new PrintWriter(writer);
		out.println("public class MyFizz4 {");
		out.println("  public static void main(String args[]) {");
		
		for (int i = 1; i< 101; i++){
			out.print("System.out.println(\"\"");
			if (i % 3 == 0) 
				out.print("+\"Fizz\"");
			if (i % 5 == 0)
				out.print("+\"Buzz\"");
			if (i % 3 != 0 && i % 5 != 0)
				out.print("+"+i);
			out.print(");");
		}
		
		
		out.println("  }");
		out.println("}");
		out.close();
		JavaFileObject file = new JavaSourceFromString("MyFizz4",
				writer.toString());

		Iterable<? extends JavaFileObject> compilationUnits = Arrays
				.asList(file);
		// ByteArrayOutputStream os_writer = new ByteArrayOutputStream();
		// Writer rez = new BufferedWriter(new OutputStreamWriter(os_writer));

		CompilationTask task = compiler.getTask(null, fileManager, diagnostics,
				null, null, compilationUnits);

		boolean success = task.call();

		for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
			System.out.println(diagnostic.getCode());
			System.out.println(diagnostic.getKind());
			System.out.println(diagnostic.getPosition());
			System.out.println(diagnostic.getStartPosition());
			System.out.println(diagnostic.getEndPosition());
			System.out.println(diagnostic.getSource());
			System.out.println(diagnostic.getMessage(null));
		}
		System.out.println("Success: " + success);

		if (success) {
			try {
				ClassLoader loader = new ByteArrayClassLoader();
				Class clazz = loader.loadClass("MyFizz4");
				clazz.getDeclaredMethod("main", new Class[] { String[].class })
						.invoke(null, new Object[] { null });

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

class JavaSourceFromString extends SimpleJavaFileObject {
	/**
	 * The source code of this "file".
	 */
	final String code;

	/**
	 * Constructs a new JavaSourceFromString.
	 * 
	 * @param name
	 *            the name of the compilation unit represented by this file
	 *            object
	 * @param code
	 *            the source code for the compilation unit represented by this
	 *            file object
	 */
	JavaSourceFromString(String name, String code) {
		super(URI.create("string:///" + name.replace('.', '/')
				+ Kind.SOURCE.extension), Kind.SOURCE);
		this.code = code;
	}

	@Override
	public CharSequence getCharContent(boolean ignoreEncodingErrors) {
		return code;
	}
}

class MemoryJavaFileObject extends SimpleJavaFileObject {
	public static ByteArrayOutputStream out = new ByteArrayOutputStream();

	public MemoryJavaFileObject(String uri, JavaFileObject.Kind kind) {
		super(java.net.URI.create(uri), kind);
	}

	@Override
	public OutputStream openOutputStream() {
		return out;
	}
}

class ByteArrayClassLoader extends ClassLoader {
	public Class findClass(String name) {
		byte[] bytes = MemoryJavaFileObject.out.toByteArray();
		return super.defineClass(name, bytes, 0, bytes.length);
	}
}
