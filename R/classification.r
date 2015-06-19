library('e1071')

#' Classify the given SpatialPointsDataFrame
#' @param proj.df SpatialPointsDataFrame, the points to classify
#' @param debug boolean, to use the debug mode
#' @return proj.done SpatialPointsDataFrame, the points classified
#' @author Valentin SASYAN
#' @version 1.0.0
#' @date  06/19/2015
classification <- function(proj.df, debug=FALSE) {

	# Separate 1) the data used as model and 2) the data to process:
	proj.model <- subset(proj.df[,c(1,2,3,5)], type != 0)
	proj.process <- subset(proj.df[,c(1,2,3,5)], type == 0)

	# Create the model for classification:
	model.data <- as.data.frame(proj.model)
	model.factor <- factor(model.data$type)

	# Learn
	model.x <- subset(model.data, select = model.factor)
	model.y <- model.factor
	model.svm <- svm(model.x, model.y)

	if (debug) {
		# Test with train data:
		model.pred <- predict(model.svm, model.x)

		# Check accuracy:
		print(table(model.pred, model.y))
	}

	# Predict the data to process:
	process.data <- as.data.frame(proj.process)
	process.x <- subset(process.data, select = model.factor)
	process.pred <- predict(model.svm, process.x)

	# Set the prediction in the data to process:
	proj.process$type <- process.pred

	# Return the done data:
	proj.done <- rbind(proj.model, proj.process)
}