package org.jmlspecs.jml4.ast;

import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class JmlTypeExpression extends JmlUnaryExpression {

	private /*@nullable*/ TypeBinding targetType;

	public JmlTypeExpression(TypeReference type, int op) {
		super(type, op);
	}

	public TypeBinding targetType() {
		return this.targetType;
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append("\\type("); //$NON-NLS-1$		
		this.expression.print(indent, output);
		return output.append(')');
	}

	public /*@nullable*/ TypeBinding resolveType(BlockScope scope) {
		super.resolveType(scope); // value from super ignored
		targetType = this.expression.resolvedType;
		if (targetType == null)
			return null;
		
		if (targetType.isArrayType()) {
			ArrayBinding arrayBinding = (ArrayBinding) targetType;
			TypeBinding leafComponentType = arrayBinding.leafComponentType;
			if (leafComponentType == TypeBinding.VOID) {
				// this allowed type was already reported at super.resolveType.
				return null;
			} else if (leafComponentType.isTypeVariable()) {
				scope.problemReporter().illegalClassLiteralForTypeVariable((TypeVariableBinding)leafComponentType, this);
			}
		} else if (targetType.isTypeVariable()) {
			scope.problemReporter().illegalClassLiteralForTypeVariable((TypeVariableBinding)targetType, this);
		}
		ReferenceBinding classType = scope.getJavaLangClass();
		if (classType.isGenericType()) {
			// Integer.class --> Class<Integer>, perform boxing of base types (int.class --> Class<Integer>)
			TypeBinding boxedType = null;
			if (targetType.id == T_void) {
				boxedType = scope.environment().getResolvedType(JAVA_LANG_VOID, scope);
			} else {
				boxedType = scope.boxing(targetType);
			}
			this.resolvedType = scope.environment().createParameterizedType(classType, new TypeBinding[]{ boxedType }, null/*not a member*/);
		} else {
			this.resolvedType = classType;
		}
		return this.resolvedType;
	}

}
