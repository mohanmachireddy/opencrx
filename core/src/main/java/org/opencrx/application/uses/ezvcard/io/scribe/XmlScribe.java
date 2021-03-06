package org.opencrx.application.uses.ezvcard.io.scribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;

import org.opencrx.application.uses.ezvcard.VCardDataType;
import org.opencrx.application.uses.ezvcard.VCardVersion;
import org.opencrx.application.uses.ezvcard.io.CannotParseException;
import org.opencrx.application.uses.ezvcard.io.xml.XCardElement;
import org.opencrx.application.uses.ezvcard.parameter.VCardParameters;
import org.opencrx.application.uses.ezvcard.property.Xml;
import org.opencrx.application.uses.ezvcard.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/*
 Copyright (c) 2013, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * Marshals {@link Xml} properties.
 * @author Michael Angstadt
 */
public class XmlScribe extends VCardPropertyScribe<Xml> {
	public XmlScribe() {
		super(Xml.class, "XML");
	}

	@Override
	protected VCardDataType _defaultDataType(VCardVersion version) {
		return VCardDataType.TEXT;
	}

	@Override
	protected String _writeText(Xml property, VCardVersion version) {
		Document value = property.getValue();
		if (value == null) {
			return "";
		}

		String xml = valueToString(value);
		return escape(xml);
	}

	@Override
	protected Xml _parseText(String value, VCardDataType dataType, VCardVersion version, VCardParameters parameters, List<String> warnings) {
		value = unescape(value);
		try {
			return new Xml(value);
		} catch (SAXException e) {
			throw new CannotParseException("Cannot parse value as XML: " + value);
		}
	}

	@Override
	protected void _writeXml(Xml property, XCardElement element) {
		//Xml properties are handled as a special case when writing xCard documents, so this method should never get called (see: "XCardDocument" class)
		super._writeXml(property, element);
	}

	@Override
	protected Xml _parseXml(XCardElement element, VCardParameters parameters, List<String> warnings) {
		Xml xml = new Xml(element.element());

		//remove the <parameters> element
		Element root = XmlUtils.getRootElement(xml.getValue());
		for (Element child : XmlUtils.toElementList(root.getChildNodes())) {
			if ("parameters".equals(child.getLocalName()) && VCardVersion.V4_0.getXmlNamespace().equals(child.getNamespaceURI())) {
				root.removeChild(child);
			}
		}

		return xml;
	}

	private String valueToString(Document document) {
		Map<String, String> props = new HashMap<String, String>();
		props.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
		return XmlUtils.toString(document, props);
	}
}