{{- define "interview-coach.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "interview-coach.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name (include "interview-coach.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{- define "interview-coach.labels" -}}
app.kubernetes.io/name: {{ include "interview-coach.name" . }}
helm.sh/chart: {{ .Chart.Name }}-{{ .Chart.Version | replace "+" "_" }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{- define "interview-coach.selectorLabels" -}}
app.kubernetes.io/name: {{ include "interview-coach.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{- define "interview-coach.appSecretName" -}}
{{ include "interview-coach.fullname" . }}-app-secret
{{- end -}}

{{- define "interview-coach.appConfigName" -}}
{{ include "interview-coach.fullname" . }}-app-config
{{- end -}}

{{- define "interview-coach.postgresSecretName" -}}
{{ include "interview-coach.fullname" . }}-postgres-secret
{{- end -}}

{{- define "interview-coach.postgresHost" -}}
{{ include "interview-coach.fullname" . }}-postgres
{{- end -}}
