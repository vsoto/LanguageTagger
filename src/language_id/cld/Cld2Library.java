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

package language_id.cld;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.ptr.PointerByReference;


/**
 * JNA Wrapper for library <b>Cld2</b><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that
 * <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource
 * projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a>,
 * <a href="http://rococoa.dev.java.net/">Rococoa</a>, or
 * <a href="http://jna.dev.java.net/">JNA</a>.
 */
interface Cld2Library extends Library {
  String JNA_LIBRARY_NAME = "cld2_full";
  NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(Cld2Library.JNA_LIBRARY_NAME);
  Cld2Library INSTANCE = (Cld2Library) Native.loadLibrary(Cld2Library.JNA_LIBRARY_NAME,
                                                          Cld2Library.class);


  //String LanguageName(int lang);
  String _ZN4CLD212LanguageNameENS_8LanguageE(int lang);

  //String LanguageCode(int lang);
  String _ZN4CLD212LanguageCodeENS_8LanguageE(int lang);

  //int GetLanguageFromName(String src);
  int _ZN4CLD219GetLanguageFromNameEPKc(String src);

  //int ExtDetectLanguageSummary(String buffer, int buffer_length, byte is_plain_text, CLDHints cld_hints, int flags, IntBuffer language3, IntBuffer percent3, DoubleBuffer normalized_score3, PointerByReference resultchunkvector, IntBuffer text_bytes, ByteBuffer is_reliable);
  int _ZN4CLD224ExtDetectLanguageSummaryEPKcibPKNS_8CLDHintsEiPNS_8LanguageEPiPdPSt6vectorINS_11ResultChunkESaISA_EES7_Pb(
          byte[] buffer, int bufferLength, boolean isPlainText, CLDHints cldHints, int flags,
          int[] language3, int[] percent3, double[] normalizedScore3,
          PointerByReference resultchunkvector, int[] textBytes, boolean[] isReliable);

  //String DetectLanguageVersion();
  String _ZN4CLD221DetectLanguageVersionEv();
}
