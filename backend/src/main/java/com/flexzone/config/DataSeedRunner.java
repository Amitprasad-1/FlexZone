package com.flexzone.config;

import com.flexzone.entity.*;
import com.flexzone.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class DataSeedRunner implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TrainerProfileRepository trainerProfileRepository;

    @Autowired
    private MembershipPlanRepository membershipPlanRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberProfileRepository memberProfileRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private BmiLogRepository bmiLogRepository;

    @Autowired
    private ClassScheduleRepository classScheduleRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 1. Seed admin user specifically if the "admin" username is missing
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .email("admin@flexzone.com")
                    .fullName("System Admin")
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println(">>> Database Seeded: Admin credentials set to (admin / admin123)");
        }

        // 1.b Seed trainer accounts if they are missing
        if (userRepository.findByUsername("vikram").isEmpty()) {
            User vikramUser = User.builder()
                    .username("vikram")
                    .password(passwordEncoder.encode("trainer123"))
                    .email("vikram@flexzone.com")
                    .fullName("Vikram Singh")
                    .role(Role.TRAINER)
                    .build();
            User savedVikram = userRepository.save(vikramUser);
            
            TrainerProfile vikramProfile = TrainerProfile.builder()
                    .user(savedVikram)
                    .specialization("Head Strength Coach")
                    .bio("Vikram specializes in heavy lifts, strength conditioning, and bodybuilding prep.||trainer1.png")
                    .experienceYears(8)
                    .build();
            trainerProfileRepository.save(vikramProfile);
        }

        if (userRepository.findByUsername("pooja").isEmpty()) {
            User poojaUser = User.builder()
                    .username("pooja")
                    .password(passwordEncoder.encode("trainer123"))
                    .email("pooja@flexzone.com")
                    .fullName("Pooja Sharma")
                    .role(Role.TRAINER)
                    .build();
            User savedPooja = userRepository.save(poojaUser);
            
            TrainerProfile poojaProfile = TrainerProfile.builder()
                    .user(savedPooja)
                    .specialization("Yoga & Zumba Instructor")
                    .bio("Pooja blends flexibility workouts with aerobic zumba for cardio endurance.||trainer2.png")
                    .experienceYears(5)
                    .build();
            trainerProfileRepository.save(poojaProfile);
        }

        if (userRepository.findByUsername("rajesh").isEmpty()) {
            User rajeshUser = User.builder()
                    .username("rajesh")
                    .password(passwordEncoder.encode("trainer123"))
                    .email("rajesh@flexzone.com")
                    .fullName("Rajesh Sen")
                    .role(Role.TRAINER)
                    .build();
            User savedRajesh = userRepository.save(rajeshUser);
            
            TrainerProfile rajeshProfile = TrainerProfile.builder()
                    .user(savedRajesh)
                    .specialization("Nutrition & Transformation Expert")
                    .bio("Rajesh focuses on functional fitness and custom meal planning.||trainer3.png")
                    .experienceYears(6)
                    .build();
            trainerProfileRepository.save(rajeshProfile);
            System.out.println(">>> Database Seeded: Trainer accounts vikram, pooja, rajesh populated.");
        }

        // 2. Seed membership plans
        if (membershipPlanRepository.count() == 0) {
            membershipPlanRepository.saveAll(Arrays.asList(
                    MembershipPlan.builder().name("Monthly Starter").description("Access to gym area and general training.").price(BigDecimal.valueOf(1500.00)).durationDays(30).build(),
                    MembershipPlan.builder().name("Quarterly Fit").description("Includes group class entry and personal training review.").price(BigDecimal.valueOf(4000.00)).durationDays(90).build(),
                    MembershipPlan.builder().name("Yearly Elite").description("Unlimited access to all classes, personal training, steam rooms.").price(BigDecimal.valueOf(12000.00)).durationDays(365).build()
            ));
            System.out.println(">>> Database Seeded: Membership plans populated.");
        }

        // 3. Seed hourly time slots
        if (timeSlotRepository.count() == 0) {
            timeSlotRepository.saveAll(Arrays.asList(
                    TimeSlot.builder().startTime(LocalTime.of(6, 0)).endTime(LocalTime.of(7, 0)).maxCapacity(25).build(),
                    TimeSlot.builder().startTime(LocalTime.of(7, 0)).endTime(LocalTime.of(8, 0)).maxCapacity(25).build(),
                    TimeSlot.builder().startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(9, 0)).maxCapacity(25).build(),
                    TimeSlot.builder().startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(10, 0)).maxCapacity(20).build(),
                    TimeSlot.builder().startTime(LocalTime.of(16, 0)).endTime(LocalTime.of(17, 0)).maxCapacity(25).build(),
                    TimeSlot.builder().startTime(LocalTime.of(17, 0)).endTime(LocalTime.of(18, 0)).maxCapacity(30).build(),
                    TimeSlot.builder().startTime(LocalTime.of(18, 0)).endTime(LocalTime.of(19, 0)).maxCapacity(30).build(),
                    TimeSlot.builder().startTime(LocalTime.of(19, 0)).endTime(LocalTime.of(20, 0)).maxCapacity(30).build(),
                    TimeSlot.builder().startTime(LocalTime.of(20, 0)).endTime(LocalTime.of(21, 0)).maxCapacity(25).build()
            ));
            System.out.println(">>> Database Seeded: Hourly gym slots populated.");
        }

        // 4. Seed shop catalog products
        if (productRepository.count() == 0) {
            productRepository.saveAll(Arrays.asList(
                    Product.builder().name("Premium Whey Protein (1kg)").description("Ultra-filtered premium chocolate whey protein concentrate.").price(BigDecimal.valueOf(2800.00)).imageUrl("whey_protein.jpg").stock(50).build(),
                    Product.builder().name("Ergonomic Gym Shaker (700ml)").description("Leak-proof protein shaker bottle with mixing ball.").price(BigDecimal.valueOf(350.00)).imageUrl("gym_shaker.jpg").stock(120).build(),
                    Product.builder().name("Pre-Workout Explode (300g)").description("High-energy powder formulation for intense pump.").price(BigDecimal.valueOf(1900.00)).imageUrl("pre_workout.jpg").stock(40).build(),
                    Product.builder().name("L-Carnitine Liquid (500ml)").description("Supports energy metabolism and fat loss.").price(BigDecimal.valueOf(1200.00)).imageUrl("l_carnitine.jpg").stock(30).build(),
                    Product.builder().name("Comfort Fit Gym Gloves").description("Padded training gloves with wrist support wrap.").price(BigDecimal.valueOf(450.00)).imageUrl("gym_gloves.jpg").stock(80).build()
            ));
            System.out.println(">>> Database Seeded: E-Shop catalog items populated.");
        }

        // 5. Seed more trainers to reach at least 25
        long currentTrainersCount = trainerProfileRepository.count();
        if (currentTrainersCount < 25) {
            String[] firstNames = {
                "Arjun", "Neha", "Kabir", "Rohan", "Anjali", "Siddharth", "Preeti", "Aditya", "Riya", "Karan", 
                "Simran", "Amit", "Priya", "Rahul", "Tanvi", "Yash", "Kiran", "Vijay", "Divya", "Sanjay", 
                "Meera", "Deepak", "Asha"
            };
            String[] lastNames = {
                "Singh", "Sharma", "Sen", "Gupta", "Malhotra", "Verma", "Kumar", "Patel", "Roy", "Das",
                "Joshi", "Mehta", "Bose", "Nair", "Reddy", "Rao", "Mishra", "Tiwari", "Yadav", "Prasad",
                "Sinha", "Saxena", "Bhatt"
            };
            String[] specs = {
                "HIIT & Cardio Specialist", "Powerlifting Coach", "Calisthenics & Bodyweight Coach", 
                "Yoga & Zumba Instructor", "Pilates & Core Instructor", "Nutrition & Weight Management Expert",
                "CrossFit & Functional Training Coach", "MMA & Kickboxing Trainer", "Physiotherapist & Rehab Coach",
                "Spin & Indoor Cycling Instructor"
            };
            String[] bioTemplates = {
                "Passionate about teaching high-intensity classes that burn fat and build stamina.",
                "Focused on compound movements, heavy lifts, and progressive overload training.",
                "Teaches bodyweight control, core stability, and agility through natural movement.",
                "Combines dance, music, and stretching to deliver an energetic, full-body workout.",
                "Specializes in low-impact flexibility and muscular endurance sessions for longevity.",
                "Enjoys helping clients build sustainable diet habits and achieve body transformation.",
                "Builds endurance and work capacity through functional variety and high-rep workouts.",
                "Incorporates martial arts, combat conditioning, and self-defense into custom routines.",
                "Guides recovery, corrective exercise, and mobility to prevent injury and restore function.",
                "Leads high-energy cycling rides with positive motivation and cardiovascular focus."
            };

            Random random = new Random();
            int trainersNeeded = 25 - (int) currentTrainersCount;
            for (int i = 0; i < trainersNeeded; i++) {
                String fName = firstNames[i % firstNames.length];
                String lName = lastNames[i % lastNames.length];
                String fullName = fName + " " + lName;
                String username = fName.toLowerCase() + (i + 1);
                
                if (userRepository.findByUsername(username).isPresent()) {
                    continue;
                }

                User trainerUser = User.builder()
                        .username(username)
                        .password(passwordEncoder.encode("trainer123"))
                        .email(username + "@flexzone.com")
                        .fullName(fullName)
                        .role(Role.TRAINER)
                        .build();
                User savedUser = userRepository.save(trainerUser);

                String spec = specs[random.nextInt(specs.length)];
                String bioText = bioTemplates[random.nextInt(bioTemplates.length)];
                int photoNum = (random.nextInt(3) + 1);
                String bio = bioText + "||trainer" + photoNum + ".png";

                TrainerProfile profile = TrainerProfile.builder()
                        .user(savedUser)
                        .specialization(spec)
                        .bio(bio)
                        .experienceYears(3 + random.nextInt(12))
                        .build();
                trainerProfileRepository.save(profile);
            }
            System.out.println(">>> Database Seeded: Added trainers to reach a total of 25.");
        }

        // 6. Seed 100 members with realistic details
        long currentMembersCount = memberProfileRepository.count();
        if (currentMembersCount == 0) {
            List<MembershipPlan> plans = membershipPlanRepository.findAll();
            List<TrainerProfile> trainers = trainerProfileRepository.findAll();
            List<TimeSlot> slots = timeSlotRepository.findAll();
            
            if (!plans.isEmpty() && !trainers.isEmpty()) {
                String[] firstNames = {
                    "Aarav", "Ananya", "Vivaan", "Diya", "Reyansh", "Ira", "Vihaan", "Kiara", "Arjun", "Aadhya", 
                    "Sai", "Ishaan", "Saanvi", "Krishna", "Pranav", "Rhea", "Kabir", "Meera", "Aaryan", "Sara", 
                    "Dev", "Tanya", "Rohan", "Avani", "Veer", "Myra", "Siddharth", "Kavya", "Ishita", "Aryan", 
                    "Pooja", "Sneha", "Aditi", "Rahul", "Nikhil", "Akash", "Varun", "Shruti", "Neha", "Kunal", 
                    "Ridhi", "Samarth", "Simran", "Preeti", "Gaurav", "Shreya", "Karan", "Tanvi", "Sanjay", 
                    "Asha", "Vijay", "Kiran", "Deepak", "Jyoti", "Ravi", "Sunita", "Manoj", "Anita", "Geeta", 
                    "Anil", "Rekha", "Harish", "Babita", "Suresh", "Lata", "Ramesh", "Meena", "Naresh", "Seema", 
                    "Dinesh", "Kusum", "Jitendra", "Sharda", "Kamal", "Maya", "Chandra", "Sushma", 
                    "Arvind", "Pushpa", "Ashok", "Kanta", "Satish", "Vimla", "Subhash", "Savitri", "Mahendra", 
                    "Shanti", "Vinod", "Sarla", "Pramod", "Sudha", "Narendra", "Vidya", "Kartik", "Kirti", "Umesh"
                };
                String[] lastNames = {
                    "Sharma", "Verma", "Singh", "Gupta", "Kumar", "Joshi", "Mehta", "Patel", "Shah", "Sen", 
                    "Roy", "Das", "Banerjee", "Chatterjee", "Mukherjee", "Bose", "Nair", "Pillai", "Iyer", "Iyengar", 
                    "Reddy", "Rao", "Choudhury", "Mishra", "Pandey", "Trivedi", "Dwivedi", "Dubey", "Shukla", "Tiwari", 
                    "Yadav", "Prasad", "Sinha", "Saxena", "Srivastava", "Kapoor", "Khanna", "Malhotra", "Bhatt"
                };

                Random random = new Random();
                List<MemberProfile> seededMembers = new ArrayList<>();

                for (int i = 0; i < 100; i++) {
                    String fName = firstNames[i % firstNames.length];
                    String lName = lastNames[random.nextInt(lastNames.length)];
                    String fullName = fName + " " + lName;
                    String username = fName.toLowerCase() + "." + lName.toLowerCase() + (i + 1);

                    User user = User.builder()
                            .username(username)
                            .password(passwordEncoder.encode("member123"))
                            .email(username + "@gmail.com")
                            .fullName(fullName)
                            .role(Role.MEMBER)
                            .build();
                    User savedUser = userRepository.save(user);

                    String status;
                    if (i < 70) {
                        status = "ACTIVE";
                    } else if (i < 85) {
                        status = "EXPIRED";
                    } else {
                        status = "PENDING";
                    }

                    MembershipPlan plan;
                    double r = random.nextDouble();
                    if (r < 0.40) {
                        plan = plans.get(0);
                    } else if (r < 0.80) {
                        plan = plans.size() > 1 ? plans.get(1) : plans.get(0);
                    } else {
                        plan = plans.size() > 2 ? plans.get(2) : plans.get(0);
                    }

                    TrainerProfile assignedTrainer = null;
                    if (random.nextDouble() < 0.80) {
                        assignedTrainer = trainers.get(random.nextInt(trainers.size()));
                    }

                    LocalDate startDate = null;
                    LocalDate endDate = null;

                    if ("ACTIVE".equals(status)) {
                        startDate = LocalDate.now().minusDays(random.nextInt(50) + 10);
                        endDate = startDate.plusDays(plan.getDurationDays());
                        if (endDate.isBefore(LocalDate.now())) {
                            endDate = LocalDate.now().plusDays(random.nextInt(30) + 5);
                        }
                    } else if ("EXPIRED".equals(status)) {
                        endDate = LocalDate.now().minusDays(random.nextInt(40) + 5);
                        startDate = endDate.minusDays(plan.getDurationDays());
                    } else {
                        startDate = LocalDate.now().plusDays(random.nextInt(10) + 1);
                        endDate = startDate.plusDays(plan.getDurationDays());
                    }

                    MemberProfile profile = MemberProfile.builder()
                            .user(savedUser)
                            .membershipStatus(status)
                            .membershipPlan(plan)
                            .membershipStartDate(startDate)
                            .membershipEndDate(endDate)
                            .assignedTrainer(assignedTrainer)
                            .build();
                    MemberProfile savedProfile = memberProfileRepository.save(profile);
                    seededMembers.add(savedProfile);

                    if ("ACTIVE".equals(status) || "EXPIRED".equals(status)) {
                        LocalDateTime paymentTime = startDate.atTime(LocalTime.of(8 + random.nextInt(12), random.nextInt(60)));
                        String method = Arrays.asList("RAZORPAY", "PAYTM", "CASH", "CARD").get(random.nextInt(4));
                        String txId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase() + "-" + i;

                        Payment payment = Payment.builder()
                                .member(savedProfile)
                                .amount(plan.getPrice())
                                .paymentDate(paymentTime)
                                .paymentType("MEMBERSHIP_RENEWAL")
                                .paymentMethod(method)
                                .transactionId(txId)
                                .status("SUCCESS")
                                .build();
                        paymentRepository.save(payment);
                    }

                    if (random.nextDouble() < 0.50 && startDate != null) {
                        BigDecimal height = BigDecimal.valueOf(155.0 + random.nextDouble() * 30.0).setScale(2, java.math.RoundingMode.HALF_UP);
                        BigDecimal weight = BigDecimal.valueOf(50.0 + random.nextDouble() * 45.0).setScale(2, java.math.RoundingMode.HALF_UP);
                        double heightM = height.doubleValue() / 100.0;
                        BigDecimal bmi = BigDecimal.valueOf(weight.doubleValue() / (heightM * heightM)).setScale(2, java.math.RoundingMode.HALF_UP);

                        BmiLog log = BmiLog.builder()
                                .member(savedProfile)
                                .heightCm(height)
                                .weightKg(weight)
                                .calculatedBmi(bmi)
                                .loggedDate(startDate)
                                .build();
                        bmiLogRepository.save(log);
                    }
                }
                System.out.println(">>> Database Seeded: Added 100 member accounts, payments, and BMI logs.");

                List<ClassSchedule> schedules = new ArrayList<>();
                String[] classNames = {"Zumba Fusion", "Vinyasa Yoga Flow", "HIIT Power Hour", "Core & Flex Pilates", "Strength & Conditioning"};
                String[] classDescriptions = {
                    "Dance fitness class combining Latin and international music with dance moves.",
                    "Fluid, movement-oriented yoga style that coordinates breathing with physical postures.",
                    "High-intensity interval training designed to maximize fat burn and boost metabolism.",
                    "Core strengthening pilates session focused on alignment, control, and deep muscle activation.",
                    "Full-body strength building using dumbbells, barbells, and functional bodyweight lifts."
                };

                for (int i = 0; i < classNames.length; i++) {
                    TrainerProfile classTrainer = trainers.get(i % trainers.size());
                    LocalDateTime classStartTime = LocalDate.now().atTime(LocalTime.of(8 + (i * 2), 0));
                    LocalDateTime classEndTime = classStartTime.plusHours(1);

                    ClassSchedule schedule = ClassSchedule.builder()
                            .className(classNames[i])
                            .description(classDescriptions[i])
                            .trainer(classTrainer)
                            .startTime(classStartTime)
                            .endTime(classEndTime)
                            .maxCapacity(20)
                            .build();
                    schedules.add(classScheduleRepository.save(schedule));
                }
                System.out.println(">>> Database Seeded: Seeded weekly Class Schedules.");

                if (!slots.isEmpty()) {
                    int bookingsCreated = 0;
                    for (int i = 0; i < seededMembers.size() && bookingsCreated < 35; i++) {
                        MemberProfile member = seededMembers.get(i);
                        if ("ACTIVE".equals(member.getMembershipStatus())) {
                            TimeSlot slot = slots.get(random.nextInt(slots.size()));
                            long currentBookings = bookingRepository.countByTimeSlotAndBookingDate(slot, LocalDate.now());
                            if (currentBookings < slot.getMaxCapacity()) {
                                Booking booking = Booking.builder()
                                        .member(member)
                                        .bookingType("SLOT")
                                        .timeSlot(slot)
                                        .bookingDate(LocalDate.now())
                                        .build();
                                try {
                                    bookingRepository.save(booking);
                                    bookingsCreated++;
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                    System.out.println(">>> Database Seeded: Created " + bookingsCreated + " today's slot bookings.");
                }

                int classRegistrations = 0;
                for (int i = 0; i < seededMembers.size() && classRegistrations < 25; i++) {
                    MemberProfile member = seededMembers.get(i);
                    if ("ACTIVE".equals(member.getMembershipStatus())) {
                        ClassSchedule schedule = schedules.get(random.nextInt(schedules.size()));
                        long currentBookings = bookingRepository.countByClassSchedule(schedule);
                        if (currentBookings < schedule.getMaxCapacity()) {
                            Booking booking = Booking.builder()
                                    .member(member)
                                    .bookingType("CLASS")
                                    .classSchedule(schedule)
                                    .bookingDate(schedule.getStartTime().toLocalDate())
                                    .build();
                            try {
                                bookingRepository.save(booking);
                                classRegistrations++;
                            } catch (Exception ignored) {}
                        }
                    }
                }
                System.out.println(">>> Database Seeded: Created " + classRegistrations + " class bookings.");

                User adminUser = userRepository.findByUsername("admin").orElse(null);
                if (adminUser != null) {
                    int checkInsCreated = 0;
                    for (int i = 0; i < seededMembers.size() && checkInsCreated < 8; i++) {
                        MemberProfile member = seededMembers.get(i);
                        if ("ACTIVE".equals(member.getMembershipStatus())) {
                            Attendance att = Attendance.builder()
                                    .member(member)
                                    .checkInTime(LocalDateTime.now().minusMinutes(random.nextInt(240) + 10))
                                    .verifiedByAdmin(adminUser)
                                    .build();
                            attendanceRepository.save(att);
                            checkInsCreated++;
                        }
                    }
                    System.out.println(">>> Database Seeded: Seeded " + checkInsCreated + " today check-in records.");
                }
            }
        }
    }
}
