package org.openmrs.module.appointments.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.email.notification.EmailNotificationException;
import org.bahmni.module.email.notification.service.EmailNotificationService;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.context.Context;
import org.openmrs.module.appointments.model.Appointment;
import org.openmrs.module.appointments.model.AppointmentProvider;
import org.openmrs.module.appointments.service.TeleconsultationAppointmentNotificationService;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TeleconsultationAppointmentNotificationServiceImpl implements TeleconsultationAppointmentNotificationService {
    private final static String EMAIL_SUBJECT = "teleconsultation.appointment.email.subject";
    private final static String EMAIL_BODY = "teleconsultation.appointment.email.body";
    private final static String EMAIL_LOGO = "teleconsultation.appointment.email.logo";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private Log log = LogFactory.getLog(this.getClass());

    private EmailNotificationService emailNotificationService;

    private EmailTemplateConfig emailTemplateConfig = new EmailTemplateConfig();

    private TeleconsultationAppointmentService teleconsultationAppointmentService = new TeleconsultationAppointmentService();

    public TeleconsultationAppointmentNotificationServiceImpl() {}
    public TeleconsultationAppointmentNotificationServiceImpl(
            EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    public void sendTeleconsultationAppointmentLinkEmail(Appointment appointment) throws EmailNotificationException {
        String link = teleconsultationAppointmentService.getTeleconsultationURL(appointment);
        Patient patient = appointment.getPatient();
        PersonAttribute patientEmailAttribute = patient.getAttribute("email");
        try {
            if (patientEmailAttribute != null) {
                String email = patientEmailAttribute.getValue();
                String patientName = appointment.getPatient().getGivenName();
                String doctor = "";
                if (appointment.getProviders() != null) {
                    AppointmentProvider provider = appointment.getProviders().iterator().next();
                    doctor = " with Dr. " + provider.getProvider().getPerson().getGivenName()+" "+provider.getProvider().getPerson().getFamilyName();
                }
                Date appointmentStart = appointment.getStartDateTime();
                SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
                LocalDateTime now = LocalDateTime.now();
                ZoneId zone = ZoneId.systemDefault();
                ZoneOffset zoneOffSet = zone.getRules().getOffset(now);
                String localString = sdf.format(appointmentStart) + zoneOffSet.toString() + "[" + zone.toString() + "]";
                ZonedDateTime localZonedDateTime = ZonedDateTime.parse(localString);
                ZoneId destZone = ZoneId.of(appointment.getTimezone());
                ZonedDateTime destZonedDateTime = localZonedDateTime.withZoneSameInstant(destZone);

                String day = destZonedDateTime.format(DateTimeFormatter.ofPattern("EEEE"));
                String date = destZonedDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yy"));
                String time = destZonedDateTime.format(DateTimeFormatter.ofPattern("hh:mm a z"));

                Properties properties = emailTemplateConfig.getProperties();
                String emailSubject = null;
                String emailBody = null;
                String emailLogo = null;
                if (properties != null) {
                    emailBody = properties.getProperty("email.body");
                    emailSubject = properties.getProperty("email.subject");
                    emailLogo = properties.getProperty("email.logo");
                }
                else {
                    emailBody = EMAIL_BODY;
                    emailSubject = EMAIL_SUBJECT;
                    emailLogo = EMAIL_LOGO;
                }

                emailNotificationService.send(
                        Context.getMessageSourceService().getMessage(emailSubject, new Object[]{doctor}, null),
                        Context.getMessageSourceService().getMessage(
                                emailBody,
                                new Object[]{
                                        patientName,
                                        doctor,
                                        day,
                                        date,
                                        time,
                                        link
                                },
                                null
                        ),
                        new String[]{email},
                        null,
                        null,
                        Context.getMessageSourceService().getMessage(emailLogo, null, null));
            } else {
                log.warn("Attempting to send an email to a patient without an email address");
            }
        }catch (IOException e){
            throw new EmailNotificationException("Unable to load email-notification.properties, see details in README", e);
        }
    }

    public void setEmailNotificationService(EmailNotificationService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

}
