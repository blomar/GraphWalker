/*
 * #%L
 * GraphWalker Maven Plugin
 * %%
 * Copyright (C) 2011 - 2013 GraphWalker
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */
package org.graphwalker.maven.plugin.source;

import japa.parser.ASTHelper;
import japa.parser.JavaParser;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.LineComment;
import japa.parser.ast.PackageDeclaration;
import japa.parser.ast.body.*;
import japa.parser.ast.expr.*;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.graphwalker.core.model.Model;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.*;

public class CodeGenerator extends VoidVisitorAdapter {

    private final SourceFile sourceFile;
    private final Model model;

    public CodeGenerator(SourceFile sourceFile, Model model) {
        this.sourceFile = sourceFile;
        this.model = model;
    }

    public String generate() {
        CompilationUnit compilationUnit = getCompilationUnit();
        ChangeContext changeContext = new ChangeContext(model);
        visit(compilationUnit, changeContext);
        removeMethods(compilationUnit, changeContext);
        generateMethods(compilationUnit, changeContext);
        return compilationUnit.toString();
    }

    private CompilationUnit getCompilationUnit() {
        CompilationUnit compilationUnit;
        if (sourceFile.getOutputFile().exists()) {
            try {
                compilationUnit = JavaParser.parse(sourceFile.getOutputFile());
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        } else {
            compilationUnit = new CompilationUnit();
            compilationUnit.setComment(new LineComment(" Generated by GraphWalker (http://www.graphwalker.org)"));
            if (!"".equals(sourceFile.getPath())) {
                compilationUnit.setPackage(getPackageName(sourceFile));
            }
            compilationUnit.setImports(Arrays.asList(new ImportDeclaration(new NameExpr("org.graphwalker.core.annotations.Model"), false, false)));
            ASTHelper.addTypeDeclaration(compilationUnit, getInterfaceName(sourceFile));
        }
        return compilationUnit;
    }

    private void removeMethods(CompilationUnit compilationUnit, ChangeContext changeContext) {
        if (0<changeContext.getMethodDeclarations().size()) {
            ClassOrInterfaceDeclaration body = (ClassOrInterfaceDeclaration)compilationUnit.getTypes().get(0);
            body.getMembers().removeAll(changeContext.getMethodDeclarations());
        }
    }

    private void generateMethods(CompilationUnit compilationUnit, ChangeContext changeContext) {
        ClassOrInterfaceDeclaration body = (ClassOrInterfaceDeclaration)compilationUnit.getTypes().get(0);
        for (String methodName: changeContext.getMethodsName()) {
            MethodDeclaration method = new MethodDeclaration(Modifier.INTERFACE, ASTHelper.VOID_TYPE, methodName);
            ASTHelper.addMember(body, method);
        }
    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, Object object) {
        if (object instanceof ChangeContext) {
            ChangeContext changeContext = (ChangeContext)object;
            if (changeContext.getMethodsName().contains(methodDeclaration.getName())) {
                changeContext.getMethodsName().remove(methodDeclaration.getName());
            } else {
                changeContext.addMethodDeclaration(methodDeclaration);
            }
        }
    }

    private PackageDeclaration getPackageName(SourceFile sourceFile) {
        return new PackageDeclaration(ASTHelper.createNameExpr(sourceFile.getPath().replaceAll(File.separator, ".")));
    }

    private ClassOrInterfaceDeclaration getInterfaceName(SourceFile sourceFile) {
        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = new ClassOrInterfaceDeclaration(ModifierSet.PUBLIC, false, sourceFile.getBaseName());
        List<MemberValuePair> memberValuePairs = new ArrayList<MemberValuePair>();
        memberValuePairs.add(new MemberValuePair("file", new StringLiteralExpr(sourceFile.getFilename())));
        memberValuePairs.add(new MemberValuePair("type", new StringLiteralExpr(sourceFile.getExtension())));
        List<AnnotationExpr> annotations = new ArrayList<AnnotationExpr>();
        annotations.add(new NormalAnnotationExpr(ASTHelper.createNameExpr("Model"), memberValuePairs));
        classOrInterfaceDeclaration.setAnnotations(annotations);
        classOrInterfaceDeclaration.setInterface(true);
        return classOrInterfaceDeclaration;
    }
}