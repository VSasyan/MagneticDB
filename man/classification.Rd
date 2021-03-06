% Generated by roxygen2 (4.1.1): do not edit by hand
% Please edit documentation in R/classification.r
\name{classification}
\alias{classification}
\title{Classify the given SpatialPointsDataFrame}
\usage{
classification(proj.df, useX = FALSE, lessType = FALSE, rePredict = FALSE,
  debug = FALSE)
}
\arguments{
\item{proj.df}{SpatialPointsDataFrame, the points to classify}

\item{useX}{boolean, use axe X for the classification}

\item{lessType}{integer, 0 : no merge, 1 : merge similar types, 2 : merge "wood / not wood"}

\item{rePredict}{boolean, the known types can be modified during the classification}

\item{debug}{boolean, debug mode (more printed information)}
}
\value{
SpatialPointsDataFrame, the points classified
}
\description{
Classify the given SpatialPointsDataFrame
}
\author{
Valentin SASYAN, v. 2.3.0, 07/24/2015
}

