package command;

import framework.command.RunnableCommand;
import framework.exception.LaboratoryFrameworkException;
import framework.state.ApplicationState;
import framework.state.ApplicationStateAware;
import framework.utils.ConsoleUtils;
import framework.utils.MatrixUtils;
import framework.utils.ValidationUtils;
import framework.utils.VectorUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import util.EquationCommandUtils;

public class GaussCommand implements RunnableCommand, ApplicationStateAware {

    private ApplicationState applicationState;

    @Override
    public void execute(String[] strings) {
        try {
            EquationCommandUtils.assertParametersSanity(this.applicationState);
        } catch (LaboratoryFrameworkException e) {
            ConsoleUtils.println(e.getMessage());
            return;
        }
        Array2DRowRealMatrix matrix = (Array2DRowRealMatrix) applicationState.getVariable("matrix");
        ArrayRealVector vector = (ArrayRealVector) applicationState.getVariable("vector");
        ConsoleUtils.println("Before:");
        ConsoleUtils.printSystemOfLinearEquations(2, matrix, vector);
        solve(matrix, vector);
        ConsoleUtils.println("After:");
        ConsoleUtils.printSystemOfLinearEquations(2, matrix, vector);
    }

    private static void solve(Array2DRowRealMatrix matrix, ArrayRealVector vector) {
        for (int columnIndex = 0; columnIndex < matrix.getColumnDimension(); columnIndex++) {
            int rowIndex = getIndexOfMaxByModule(matrix, columnIndex);
            divideRowByMaxElement(matrix, vector, rowIndex, columnIndex);
            swapRowsOfEquation(matrix, vector, rowIndex, columnIndex);
            rowIndex = columnIndex;
            subtractRowFromOthersToMakeZeroesInColumn(matrix, vector, rowIndex, columnIndex);
        }
    }

    private static void subtractRowFromOthersToMakeZeroesInColumn(RealMatrix matrix, RealVector vector, int rowIndex, int columnIndex) {
        RealVector rowToBeSubtracted = matrix.getRowVector(rowIndex);
        for (int row = 0; row < matrix.getRowDimension(); row++) {
            if (row == rowIndex) {
                continue;
            }
            RealVector rowFromWhichWeSubtract = matrix.getRowVector(row);
            double factor = rowFromWhichWeSubtract.getEntry(columnIndex);
            RealVector subtractedRow = rowToBeSubtracted.mapMultiply(factor);
            RealVector result = rowFromWhichWeSubtract.subtract(subtractedRow);
            matrix.setRow(row, result.toArray());

            vector.setEntry(row, vector.getEntry(row) - vector.getEntry(rowIndex) * factor);
        }
    }

    private static void divideRowByMaxElement(RealMatrix matrix, RealVector vector, int rowIndex, int columnIndex) {
        RealVector row = matrix.getRowVector(rowIndex);
        double maxByModuleElement = row.getEntry(columnIndex);
        ValidationUtils.requireNotEquals(maxByModuleElement, 0.0, "Column must not consist of zeroes");
        RealVector dividedRow = row.mapDivide(maxByModuleElement);
        matrix.setRow(rowIndex, dividedRow.toArray());
        vector.setEntry(rowIndex, vector.getEntry(rowIndex) / maxByModuleElement);
    }

    private static void swapRowsOfEquation(RealMatrix matrix, RealVector vector, int index1, int index2) {
        MatrixUtils.swapRows(matrix, index1, index2);
        VectorUtils.swapCoordinates(vector, index1, index2);
    }

    private static int getIndexOfMaxByModule(Array2DRowRealMatrix matrix, int column) {
        RealVector columnVector = matrix.getColumnVector(column);
        RealVector vectorWithAbsEntries = columnVector.map(Math::abs);
        return vectorWithAbsEntries.getMaxIndex();
    }

    @Override
    public void setApplicationState(ApplicationState applicationState) {
        ValidationUtils.requireNonNull(applicationState);
        this.applicationState = applicationState;
    }
}
