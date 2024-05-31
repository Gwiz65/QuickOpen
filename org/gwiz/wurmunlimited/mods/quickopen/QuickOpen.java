/*
 * This is free and unencumbered software released into the public domain.
 *
 * Anyone is free to copy, modify, publish, use, compile, sell, or
 * distribute this software, either in source code form or as a compiled
 * binary, for any purpose, commercial or non-commercial, and by any
 * means.
 * 
 * In jurisdictions that recognize copyright laws, the author or authors
 * of this software dedicate any and all copyright interest in the
 * software to the public domain. We make this dedication for the benefit
 * of the public at large and to the detriment of our heirs and
 * successors. We intend this dedication to be an overt act of
 * relinquishment in perpetuity of all present and future rights to this
 * software under copyright law.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 * For more information, please refer to <http://unlicense.org/>
*/

package org.gwiz.wurmunlimited.mods.quickopen;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import org.gotti.wurmunlimited.modloader.classhooks.HookException;
import org.gotti.wurmunlimited.modloader.classhooks.HookManager;
import org.gotti.wurmunlimited.modloader.interfaces.Initable;
import org.gotti.wurmunlimited.modloader.interfaces.Versioned;
import org.gotti.wurmunlimited.modloader.interfaces.WurmClientMod;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

public class QuickOpen implements WurmClientMod, Initable, Versioned {

	private static final String version = "1.0";

	@Override
	public void init() {
		try {
			ClassPool hookClassPool = HookManager.getInstance().getClassPool();
			CtClass ctWurmEventHandler = hookClassPool.getCtClass("com.wurmonline.client.WurmEventHandler");
			ctWurmEventHandler.getDeclaredMethod("mousePressed").instrument(new ExprEditor() {
				public void edit(MethodCall methodCall) throws CannotCompileException {
					if (methodCall.getMethodName().equals("sendDefaultAction")) {
						methodCall.replace("{ this.world.getHud().sendAction(com."
								+ "wurmonline.shared.constants.PlayerAction.OPEN, $1); }");
					}
				}
			});
		} catch (NotFoundException | CannotCompileException e) {
			appendToFile(e);
			throw new HookException(e);
		}
	}

	// For anyone modding the client, this is seriously useful. It will write in
	// exception.txt if the code can't be injected in the client before it launches.
	public static void appendToFile(Exception e) {
		try {
			FileWriter fstream = new FileWriter("exception.txt", true);
			BufferedWriter out = new BufferedWriter(fstream);
			PrintWriter pWriter = new PrintWriter(out, true);
			e.printStackTrace(pWriter);
		} catch (Exception ie) {
			throw new RuntimeException("Could not write Exception to file", ie);
		}
	}

	@Override
	public String getVersion() {
		return version;
	}
}
