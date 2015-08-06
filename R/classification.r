library('e1071')

#' Classify the given SpatialPointsDataFrame
#' @param proj.df SpatialPointsDataFrame, the points to classify
#' @param useX boolean, use axe X for the classification
#' @param lessType integer, 0 : no merge, 1 : merge similar types, 2 : merge "wood / not wood"
#' @param rePredict boolean, the known types can be modified during the classification
#' @param debug boolean, debug mode (more printed information)
#' @return SpatialPointsDataFrame, the points classified
#' @author
#' Valentin SASYAN, v. 2.3.0, 07/24/2015
classification <- function(proj.df, useX=FALSE, lessType=FALSE, rePredict=FALSE, debug=FALSE) {
	if (useX == TRUE) {axes <- c(1,2,3)} else {axes <- c(2,3)}

	# Fusion the similar types ?
	if (lessType == 1) {
		proj.df$type <- unlist(lapply(proj.df$type, function(type) {
			if (type == 2) {type <- 1}
			if (type == 4 || type == 5 || type == 6 || type == 7) {type <- 3}
			if (type == 9 || type == 10) {type <- 8}
			if (type == 12) {type <- 11}
			if (type == 14) {type <- 13}
			type
		}))
	} else if (lessType == 2) {
		proj.df$type <- unlist(lapply(proj.df$type, function(type) {
			if (type == 1 || type == 2) {type <- 1} else {type <- 2}
			type
		}))
	}

	# Separate 1) the data used as model and 2) the data to process:
	proj.model <- as.data.frame(subset(proj.df, type != 0))
	if (rePredict == TRUE) {proj.process <- as.data.frame((proj.df))}
	else {proj.process <- as.data.frame(subset(proj.df, type == 0))}

	# Create the model for classification:
	model.data <- proj.model[axes]
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
	process.data <- proj.process[axes]
	process.pred <- predict(model.svm, process.data)

	# Set the type in the data to process:
	proj.process$type <- process.pred
	
	# Tag the data:
	proj.model$svm <- 'learn'
	proj.process$svm <- 'classif'

	# Add the meta-data:
	proj.done <- rbind(proj.model, proj.process)
	proj.done$lessType <- lessType;

	# Return the done data in SpatialDF:
	proj.done <- SpatialPointsDataFrame(coords=proj.done[7:8], data=proj.done[,-c(7,8)])
}