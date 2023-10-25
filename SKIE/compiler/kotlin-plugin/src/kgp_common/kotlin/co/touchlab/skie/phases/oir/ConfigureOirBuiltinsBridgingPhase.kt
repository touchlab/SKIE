package co.touchlab.skie.phases.oir

import co.touchlab.skie.oir.element.OirClass
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirTypeParameterParent

object ConfigureOirBuiltinsBridgingPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        configureBridging()
        removeTypeParametersInSwift()
    }

    context(SirPhase.Context)
    private fun configureBridging() {
        oirBuiltins.NSError.bridgedSirClass = sirBuiltins.Swift.Error
        oirBuiltins.NSString.bridgedSirClass = sirBuiltins.Swift.String
        oirBuiltins.NSArray.bridgedSirClass = sirBuiltins.Swift.Array
        oirBuiltins.NSSet.bridgedSirClass = sirBuiltins.Swift.Set
        oirBuiltins.NSDictionary.bridgedSirClass = sirBuiltins.Swift.Dictionary
    }

    context(SirPhase.Context)
    private fun removeTypeParametersInSwift() {
        oirBuiltins.NSArray.removeTypeParametersInSwift()
        oirBuiltins.NSMutableArray.removeTypeParametersInSwift()
        oirBuiltins.NSSet.removeTypeParametersInSwift()
        oirBuiltins.NSDictionary.removeTypeParametersInSwift()
    }

    private fun OirClass.removeTypeParametersInSwift() {
        this.originalSirClass.typeParameters.toList().forEach {
            it.parent = SirTypeParameterParent.None
        }
    }
}
