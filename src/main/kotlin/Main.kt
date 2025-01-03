import aws.sdk.kotlin.runtime.auth.credentials.ProfileCredentialsProvider
import aws.sdk.kotlin.services.ec2.Ec2Client
import aws.sdk.kotlin.services.ec2.model.*
import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.model.CommandInvocationStatus
import aws.sdk.kotlin.services.ssm.model.GetCommandInvocationRequest
import aws.sdk.kotlin.services.ssm.model.SendCommandRequest
import java.util.*

suspend fun listEc2Instances(region: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    try {
        val request = DescribeInstancesRequest { }
        val response = ec2Client.describeInstances(request)

        println("EC2 Instances in region $region:")
        response.reservations?.forEach { reservation ->
            reservation.instances?.forEach { instance ->
                println(" - Instance ID: ${instance.instanceId}")
                println("   State: ${instance.state?.name}")
                println("   Type: ${instance.instanceType}")
                println("   Public DNS: ${instance.publicDnsName}")
                println("   Launch Time: ${instance.launchTime}")
                println()
            }
        }
    } catch (e: Exception) {
        println("Error fetching instances: ${e.message}")
    } finally {
        ec2Client.close()
    }
}

suspend fun listAvailableZones(region: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    try {
        val request = DescribeAvailabilityZonesRequest { }
        val response = ec2Client.describeAvailabilityZones(request)

        println("Available Zones in region $region:")
        response.availabilityZones?.forEach { zone ->
            println(" - Zone Name: ${zone.zoneName}")
            println("   State: ${zone.state}")
            println("   Zone ID: ${zone.zoneId}")
            println("   Messages: ${zone.messages?.joinToString { it.message ?: "" }}")
            println()
        }
    } catch (e: Exception) {
        println("Error fetching availability zones: ${e.message}")
    } finally {
        ec2Client.close()
    }
}

suspend fun startEc2Instance(region: String, instanceId: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    try {
        val request = StartInstancesRequest {
            this.instanceIds = listOf(instanceId)
        }
        val response = ec2Client.startInstances(request)
        println("Starting instance: $instanceId")
        println("Current state: ${response.startingInstances?.firstOrNull()?.currentState?.name}")
    } catch (e: Exception) {
        println("Error starting instance: ${e.message}")
    } finally {
        ec2Client.close()
    }
}

suspend fun listAvailableRegions(region: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    try {
        val request = DescribeRegionsRequest { }
        val response = ec2Client.describeRegions(request)

        println("Available Regions:")
        response.regions?.forEach { region ->
            println(" - Region Name: ${region.regionName}")
            println("   Endpoint: ${region.endpoint}")
            println()
        }
    } catch (e: Exception) {
        println("Error fetching regions: ${e.message}")
    } finally {
        ec2Client.close()
    }
}

suspend fun stopEc2Instance(region: String, instanceId: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    try {
        val request = StopInstancesRequest {
            this.instanceIds = listOf(instanceId)
        }
        val response = ec2Client.stopInstances(request)
        println("Stopping instance: $instanceId")
        println("Current state: ${response.stoppingInstances?.firstOrNull()?.currentState?.name}")
    } catch (e: Exception) {
        println("Error stopping instance: ${e.message}")
    } finally {
        ec2Client.close()
    }
}

suspend fun createInstance(region: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    val scanner = Scanner(System.`in`)
    try {
        print("AMI ID를 입력하세요 (예: ami-12345678) : ")
        val amiId = scanner.nextLine()

        print("생성할 인스턴스의 이름을 입력하세요 : ")
        val instanceName = scanner.nextLine()

        print("키 페어 이름을 입력하세요 (옵션) : ")
        val keyName = scanner.nextLine()

        val request = RunInstancesRequest {
            this.imageId = amiId
            this.instanceType = InstanceType.T2Micro
            this.minCount = 1
            this.maxCount = 1
            if (keyName.isNotBlank()) {
                this.keyName = keyName
            }
        }

        val response = ec2Client.runInstances(request)

        val instanceIds = response.instances?.map { it.instanceId!! } ?: emptyList()
        if (instanceIds.isNotEmpty()) {
            println("인스턴스가 생성되었습니다: $instanceIds")

            // CreateTagsRequest 인스턴스 이름 추가
            val tagRequest = CreateTagsRequest {
                this.resources = instanceIds
                this.tags = listOf(
                    Tag {
                        key = "Name"
                        value = instanceName
                    }
                )
            }
            ec2Client.createTags(tagRequest)
            println("인스턴스에 이름 태그가 추가되었습니다: $instanceName")
        } else {
            println("인스턴스를 생성하지 못했습니다.")
        }


        response.instances?.forEach { instance ->
            println(" - Instance ID: ${instance.instanceId}")
            println("   State: ${instance.state?.name}")
        }

    } catch (e: Exception) {
        println("Error creating instance: ${e.message}")
    } finally {
        ec2Client.close()
    }
}

suspend fun rebootInstance(region: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    val scanner = Scanner(System.`in`)
    try {
        print("재부팅할 인스턴스 ID를 입력하세요 : ")
        val instanceId = scanner.nextLine()

        val request = RebootInstancesRequest {
            this.instanceIds = listOf(instanceId)
        }

        ec2Client.rebootInstances(request)
        println("인스턴스가 재부팅되었습니다: $instanceId")
    } catch (e: Exception) {
        println("Error rebooting instance: ${e.message}")
    } finally {
        ec2Client.close()
    }
}

suspend fun listImages(region: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    val scanner = Scanner(System.`in`)
    try {
        val request = DescribeImagesRequest {
            this.owners = listOf("self") // 자신의 계정에서 소유한 이미지
        }

        val response = ec2Client.describeImages(request)

        if (response.images.isNullOrEmpty()) {
            println("이미지가 없습니다.")
        } else {
            println("사용 가능한 이미지 리스트:")
            response.images!!.forEach { image ->
                println(" - Image ID: ${image.imageId}")
                println("   Name: ${image.name}")
                println("   State: ${image.state}")
                println("   Description: ${image.description}")
                println()
            }
        }
    } catch (e: Exception) {
        println("Error fetching images: ${e.message}")
    } finally {
        ec2Client.close()
    }
}

suspend fun executeCommandOnInstance(region: String) {
    val ssmClient = SsmClient {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    val scanner = Scanner(System.`in`)
    try {
        print("명령을 실행할 인스턴스 ID를 입력하세요: ")
        val instanceId = scanner.nextLine()

        print("실행할 명령어를 입력하세요: ")
        val command = scanner.nextLine()

        val sendCommandRequest = SendCommandRequest {
            this.instanceIds = listOf(instanceId)
            this.documentName = "AWS-RunShellScript"
            this.parameters = mapOf("commands" to listOf(command))
        }

        val sendCommandResponse = ssmClient.sendCommand(sendCommandRequest)
        Thread.sleep(1500) // 대기 시간 추가
        val commandId = sendCommandResponse.command?.commandId

        println("명령 실행 요청 완료. Command ID: $commandId")
        println("결과를 가져오는 중...")

        // Command 실행 후 결과 조회
        if (commandId != null) {
            val resultRequest = GetCommandInvocationRequest {
                this.commandId = commandId
                this.instanceId = instanceId
            }

            while (true) {
                val resultResponse = ssmClient.getCommandInvocation(resultRequest)

                if (resultResponse.status in arrayOf(CommandInvocationStatus.InProgress, CommandInvocationStatus.Pending)) {
                    println("명령 실행 중...")
                    Thread.sleep(1000) // 대기 시간 추가
                } else {
                    println("명령 실행 완료.")
                    println("출력: ${resultResponse.standardOutputContent}")
                    println("에러: ${resultResponse.standardErrorContent}")
                    break
                }
            }
        }
    } catch (e: Exception) {
        println("Error executing command: ${e.message}")
    } finally {
        ssmClient.close()
    }
}

suspend fun createAmi(region: String) {
    val ec2Client = Ec2Client {
        this.region = region
        credentialsProvider = ProfileCredentialsProvider(profileName = "default")
    }

    val scanner = Scanner(System.`in`)
    try {
        print("AMI를 생성할 인스턴스 ID를 입력하세요: ")
        val instanceId = scanner.nextLine()

        print("생성할 AMI의 이름을 입력하세요: ")
        val amiName = scanner.nextLine()

        print("AMI 설명을 입력하세요 (옵션): ")
        val amiDescription = scanner.nextLine()

        val request = CreateImageRequest {
            this.instanceId = instanceId
            this.name = amiName
            this.description = if (amiDescription.isNotBlank()) amiDescription else null
            this.noReboot = false // 인스턴스를 재부팅하여 안전한 상태로 생성
        }

        val response = ec2Client.createImage(request)
        println("AMI 생성 요청이 완료되었습니다.")
        println("AMI ID: ${response.imageId}")
        println("생성이 완료되기까지 몇 분 정도 소요될 수 있습니다.")
    } catch (e: Exception) {
        println("Error creating AMI: ${e.message}")
    } finally {
        ec2Client.close()
    }
}


suspend fun main() {
    val region = "ap-northeast-2" // 서울 리전

    val menu = Scanner(System.`in`)
    val idString = Scanner(System.`in`)

    var number = 0;
    while (true) {
        println("--------------------------------------------")
        println("            AWS  EC2 제어 프로그램            ")
        println("--------------------------------------------")
        println("1. 인스턴스 목록 조회")
        println("2. 가용 영역 조회")
        println("3. 인스턴스 시작")
        println("4. 사용가능 리전 조회")
        println("5. 인스턴스 중지")
        println("6. 인스턴스 생성")
        println("7. 인스턴스 재부팅")
        println("8. 이미지 리스트 조회")
        println("9. 인스턴스에 명령어 실행")
        println("10. AMI 생성")
        println("99. 종료")
        println("--------------------------------------------")

        print("입력하세요 : ")

        if(menu.hasNextInt()) {
            number = menu.nextInt();
        } else {
            println("메뉴에 없는 번호입니다.")
            break
        }

        when (number) {
            1 ->  {
                listEc2Instances(region)
            }

            2 -> {
                listAvailableZones(region)
            }

            3 -> {
                print("시작할 인스턴스 ID를 입력하세요: ")
                val instanceId = menu.next()
                startEc2Instance(region, instanceId)
            }

            4 -> listAvailableRegions(region)

            5 -> {
                print("중지할 인스턴스 ID를 입력하세요: ")
                val instanceId = menu.next()
                stopEc2Instance(region, instanceId)
            }

            6 -> createInstance(region)

            7 -> rebootInstance(region)

            8 -> listImages(region)

            9 -> executeCommandOnInstance(region)

            10 -> createAmi(region)

            99 -> return
        }
    }

}
