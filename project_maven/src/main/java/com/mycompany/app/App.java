package com.mycompany.app;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.MethodDeclarationMatch;
import org.eclipse.lsp4j.jsonrpc.Launcher;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageServer;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class App {
	//
	public static void main(String[] args) throws URISyntaxException, IOException, JavaModelException {

		String path = "teste.java";

		List<String> lines = Files.readAllLines(Paths.get(path));

		String content = "";
		for (String line : lines) {
			content += line + "\n";
		}

		parse(content);

	}

	public static void parse(String str) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);

		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		cu.accept(new ASTVisitor() {

			public boolean visit(AnnotationTypeDeclaration node) {
				System.out.println("AnnotationTypeDeclaration of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(AnnotationTypeMemberDeclaration node) {
				System.out.println("AnnotationTypeMemberDeclaration of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(AnonymousClassDeclaration node) {
				System.out.println("AnonymousClassDeclaration of '" + node + "' at line"
						+ cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ArrayAccess node) {
				System.out.println("ArrayAccess of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ArrayCreation node) {
				System.out
						.println("ArrayCreation of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ArrayInitializer node) {
				System.out.println(
						"ArrayInitializer of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ArrayType node) {
				System.out.println("ArrayType of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(AssertStatement node) {
				System.out.println(
						"AssertStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(Assignment node) {
				System.out.println("Assignment of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(Block node) {
				System.out.println("Block of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(BlockComment node) {
				System.out
						.println("BlockComment of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(BooleanLiteral node) {
				System.out.println(
						"BooleanLiteral of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(BreakStatement node) {
				System.out.println(
						"BreakStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(CastExpression node) {
				System.out.println(
						"CastExpression of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(CatchClause node) {
				System.out.println("CatchClause of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(CharacterLiteral node) {
				System.out.println(
						"CharacterLiteral of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ClassInstanceCreation node) {
				System.out.println("ClassInstanceCreation of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(CompilationUnit node) {
				System.out.println(
						"CompilationUnit of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ConditionalExpression node) {
				System.out.println(
						"ConditionalExpression of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ConstructorInvocation node) {
				System.out.println(
						"ConstructorInvocation of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ContinueStatement node) {
				System.out.println(
						"ContinueStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(CreationReference node) {
				System.out.println(
						"CreationReference of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(Dimension node) {
				System.out.println("Dimension of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(DoStatement node) {
				System.out.println("DoStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(EmptyStatement node) {
				System.out.println(
						"EmptyStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(EnhancedForStatement node) {
				System.out.println(
						"EnhancedForStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(EnumConstantDeclaration node) {
				System.out.println("EnumConstantDeclaration of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(EnumDeclaration node) {
				System.out.println("EnumDeclaration of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(ExpressionMethodReference node) {
				System.out.println("ExpressionMethodReference of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(ExpressionStatement node) {
				System.out.println(
						"ExpressionStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(FieldAccess node) {
				System.out.println("FieldAccess of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(FieldDeclaration node) {
				System.out.println(
						"FieldDeclaration of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ForStatement node) {
				System.out
						.println("ForStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(IfStatement node) {
				System.out.println("IfStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ImportDeclaration node) {
				System.out.println("ImportDeclaration of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(InfixExpression node) {
				System.out.println(
						"InfixExpression of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(Initializer node) {
				System.out.println("Initializer of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(InstanceofExpression node) {
				System.out.println(
						"InstanceofExpression of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(IntersectionType node) {
				System.out.println(
						"IntersectionType of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			// public boolean visit(Javadoc node) {
			// // visit tag elements inside doc comments only if requested
			// return this.visitDocTags;
			// }

			public boolean visit(LabeledStatement node) {
				System.out.println(
						"LabeledStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(LambdaExpression node) {
				System.out.println(
						"LambdaExpression of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(LineComment node) {
				System.out.println("LineComment of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(MarkerAnnotation node) {
				System.out.println(
						"MarkerAnnotation of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(MemberRef node) {
				System.out.println("MemberRef of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(MemberValuePair node) {
				System.out.println("MemberValuePair of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(MethodRef node) {
				System.out.println("MethodRef of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(MethodRefParameter node) {
				System.out.println("MethodRefParameter of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(MethodDeclaration node) {
				System.out.println("MethodDeclaration of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(MethodInvocation node) {
				System.out.println("MethodInvocation of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(Modifier node) {
				System.out.println("Modifier of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(NameQualifiedType node) {
				System.out.println("NameQualifiedType of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(NormalAnnotation node) {
				System.out.println(
						"NormalAnnotation of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(NullLiteral node) {
				System.out.println("NullLiteral of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(NumberLiteral node) {
				System.out
						.println("NumberLiteral of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(PackageDeclaration node) {
				System.out.println("PackageDeclaration of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(ParameterizedType node) {
				System.out.println(
						"ParameterizedType of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ParenthesizedExpression node) {
				System.out.println("ParenthesizedExpression of '" + node + "' at line"
						+ cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(PostfixExpression node) {
				System.out.println(
						"PostfixExpression of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(PrefixExpression node) {
				System.out.println(
						"PrefixExpression of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(PrimitiveType node) {
				System.out
						.println("PrimitiveType of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(QualifiedName node) {
				System.out.println("QualifiedName of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(QualifiedType node) {
				System.out.println("QualifiedType of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(ReturnStatement node) {
				System.out.println(
						"ReturnStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(SimpleName node) {
				System.out.println("SimpleName of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(SimpleType node) {
				System.out.println("SimpleType of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(SingleMemberAnnotation node) {
				System.out.println(
						"SingleMemberAnnotation of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(SingleVariableDeclaration node) {
				System.out.println("SingleVariableDeclaration of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(StringLiteral node) {
				System.out
						.println("StringLiteral of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(SuperConstructorInvocation node) {
				System.out.println("SuperConstructorInvocation of '" + node + "' at line"
						+ cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(SuperFieldAccess node) {
				System.out.println("SuperFieldAccess of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(SuperMethodInvocation node) {
				System.out.println("SuperMethodInvocation of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(SuperMethodReference node) {
				System.out.println("SuperMethodReference of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(SwitchCase node) {
				System.out.println("SwitchCase of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(SwitchStatement node) {
				System.out.println(
						"SwitchStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(SynchronizedStatement node) {
				System.out.println(
						"SynchronizedStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(TagElement node) {
				System.out.println("TagElement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(TextElement node) {
				System.out.println("TextElement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ThisExpression node) {
				System.out.println(
						"ThisExpression of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(ThrowStatement node) {
				System.out.println(
						"ThrowStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(TryStatement node) {
				System.out
						.println("TryStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(TypeDeclaration node) {
				System.out.println("TypeDeclaration of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(TypeDeclarationStatement node) {
				System.out.println("TypeDeclarationStatement of '" + node + "' at line"
						+ cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(TypeLiteral node) {
				System.out.println("TypeLiteral of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(TypeMethodReference node) {
				System.out.println("TypeMethodReference of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(TypeParameter node) {
				System.out.println("TypeParameter of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(UnionType node) {
				System.out.println("UnionType of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(VariableDeclarationExpression node) {
				System.out.println("VariableDeclarationExpression of '" + node + "' at line"
						+ cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(VariableDeclarationStatement node) {
				System.out.println("VariableDeclarationStatement of '" + node + "' at line"
						+ cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(VariableDeclarationFragment node) {
				System.out.println("VariableDeclarationFragment of '" + node.getName() + "' at line"
						+ cu.getLineNumber(node.getName().getStartPosition()));
				return true;
			}

			public boolean visit(WhileStatement node) {
				System.out.println(
						"WhileStatement of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

			public boolean visit(WildcardType node) {
				System.out
						.println("WildcardType of '" + node + "' at line" + cu.getLineNumber(node.getStartPosition()));
				return true;
			}

		});
		// use ASTParse to parse string
	}

}