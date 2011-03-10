package net.sf.iqser.plugin.mail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sf.iqser.plugin.file.parser.FileParser;
import net.sf.iqser.plugin.file.parser.FileParserException;
import net.sf.iqser.plugin.file.parser.FileParserFactory;

import com.iqser.core.model.Attribute;
import com.iqser.core.model.Content;

public class ZipAttachment {

	public Collection<Content> createZipAttachments(InputStream zipIS,
			Content mailCont, String fileName, int index) throws IOException,
			FileParserException {

		String url = mailCont.getContentUrl() + fileName;
		ZipInputStream zip = new ZipInputStream(zipIS);
		ZipEntry zipEntry = null;
		Collection<Content> contents = new ArrayList<Content>();

		int off = 0;

		while ((zipEntry = zip.getNextEntry()) != null) {

			byte[] b = new byte[2048];

			int size = 0;
			if (zip.available() == 1) {

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				while ((size = zip.read(b, off, b.length)) != -1)
					bos.write(b, 0, size);

				bos.flush();
				bos.close();

				if (!zipEntry.isDirectory()) {

					String urlZipEntry = zipEntry.getName();
					Content content = null;
					byte[] byteArray = bos.toByteArray();

					InputStream is = new ByteArrayInputStream(byteArray);

					FileParserFactory parserFactory = FileParserFactory
							.getInstance();
					FileParser parser = parserFactory.getFileParser(is);

					if (parser != null) {

						String contentURL = "zip://" + url + "!/" + urlZipEntry;
						content = parser.getContent(null,
								new ByteArrayInputStream(byteArray));

						content.setContentUrl(contentURL);
						contents.add(content);
						Attribute attribute = new Attribute();
						attribute.setKey(false);
						attribute.setName("ATTACHED_TO");
						attribute.setValue(content.getContentUrl());
						content.addAttribute(attribute);

						attribute = new Attribute();
						attribute.setKey(true);
						attribute.setName("MESSAGE_ATTACHMENTS_NAME_" + index);
						attribute.setValue(contentURL);

						content.addAttribute(attribute);

					}

				}
			}

		}
		return contents;
	}
}
