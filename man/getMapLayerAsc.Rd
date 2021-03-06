% Generated by roxygen2 (4.1.1): do not edit by hand
% Please edit documentation in R/generate_qgs.r
\name{getMapLayerAsc}
\alias{getMapLayerAsc}
\title{Generate the 'maplayer' XML element of a .asc}
\usage{
getMapLayerAsc(id, datasource, layername, spatialrefsys, noData, pipe)
}
\arguments{
\item{id}{string, id of the layer}

\item{datasource}{string, datasource of the layer}

\item{layername}{string, layername of the layer}

\item{spatialrefsys}{XML element, specifications of the spatial reference systeme}

\item{noData}{XML element, number of the data}

\item{pipe}{XML element, specifications of the colorisation of the layer}
}
\value{
XMLNode, the XML element generated
}
\description{
Generate the 'maplayer' XML element of a .asc
}
\author{
Valentin SASYAN, v. 1.0.0, 06/12/2015
}

