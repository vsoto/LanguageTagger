/*
 * Copyright 2014-present Deezer.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package main.java.language_id.cld;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class CLDHints extends Structure {
	/** "mi,en" boosts Maori and English */
	public String content_language_hint;
	/** "id" boosts Indonesian */
	public String tld_hint;
	/** SJS boosts Japanese */
	public int encoding_hint;
	/**
	 * @see Language<br>
	 * ITALIAN boosts it
	 */
	public int language_hint;
	public CLDHints() {
		super();
	}
	protected List<String> getFieldOrder() {
		return Arrays.asList("content_language_hint", "tld_hint", "encoding_hint", "language_hint");
	}
	public CLDHints(String content_language_hint, String tld_hint, int encoding_hint, int language_hint) {
		super();
		this.content_language_hint = content_language_hint;
		this.tld_hint = tld_hint;
		this.encoding_hint = encoding_hint;
		this.language_hint = language_hint;
	}
	public CLDHints(Pointer peer) {
		super(peer);
	}
}
