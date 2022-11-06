package co.touchlab.skie.plugin.generator.internal.datastruct;

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor;
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1;
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory2;
import org.jetbrains.kotlin.diagnostics.Errors;
import org.jetbrains.kotlin.diagnostics.Severity;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.types.KotlinType;

public interface DataStructErrors {

    DiagnosticFactory1<KtDeclaration, DeclarationDescriptor> DATA_STRUCT_NOT_DATA_CLASS = DiagnosticFactory1.create(Severity.ERROR);
    DiagnosticFactory2<KtDeclaration, KotlinType, ValueParameterDescriptor> UNSUPPORTED_TYPE = DiagnosticFactory2.create(Severity.ERROR);

    Object _initializer = new Object() {
        {
            Errors.Initializer
                .initializeFactoryNamesAndDefaultErrorMessages(DataStructErrors.class, DataStructErrorMessages.INSTANCE);
        }
    };

}
