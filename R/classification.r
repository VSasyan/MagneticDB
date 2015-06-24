library('e1071')

#' Classify the given SpatialPointsDataFrame
#' @param proj.df SpatialPointsDataFrame, the points to classify
#' @param debug boolean, to use the debug mode
#' @return proj.done SpatialPointsDataFrame, the points classified
#' @author Valentin SASYAN
#' @version 2.0.0
#' @date  06/24/2015
classification <- function(proj.df, debug=FALSE) {

	# Separate 1) the data used as model and 2) the data to process:
	proj.model <- as.data.frame(subset(proj.df, type != 0))
	proj.process <- as.data.frame(subset(proj.df, type == 0))

	# Create the model for classification:
	model.data <- proj.model[c(1,2)]
	model.factor <- factor(proj.model$type)

	# Learn
	model.svm <- svm(model.data, model.factor)

	if (debug) {
		# Test with train data:
		model.pred <- predict(model.svm, model.data)

		# Check accuracy:
		print(table(model.pred, model.factor))
	}

	# Predict the data to process:
	process.data <- proj.process[c(1,2)]
	process.pred <- predict(model.svm, process.data)

	# Set the type in the data to process:
	proj.process$type <- process.pred
	
	# Tag the data:
	proj.model$svm = 'learn'
	proj.process$svm = 'classif'

	# Add the meta-data:
	proj.done <- rbind(proj.model, proj.process)

	# Return the done data in SpatialDF:
	proj.done <- SpatialPointsDataFrame(coords=proj.done[9:10], data=proj.done[,-c(9,10)])
}